package com.example.focusgate.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.focusgate.BuildConfig
import com.example.focusgate.domain.model.AnkiButton
import com.example.focusgate.domain.model.ProtectedApp
import com.example.focusgate.domain.model.UnlockResult
import com.example.focusgate.domain.usecase.ObservePointsConfigUseCase
import com.example.focusgate.domain.usecase.ObserveProtectedAppsUseCase
import com.example.focusgate.domain.usecase.RecordAnkiAnswerUseCase
import com.example.focusgate.domain.usecase.RequestAppUnlockUseCase
import com.example.focusgate.ui.overlay.BlockedOverlayState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Único AccessibilityService de la app. Escucha eventos de TODAS las apps
 * (ver justificación en accessibility_service_config.xml) y hace dos
 * cosas completamente independientes con ellos:
 *
 * 1. Si el evento viene de AnkiDroid y es un click -> intenta mapear el
 *    botón pulsado (Again/Hard/Good/Easy) y registra los puntos.
 * 2. Si el evento es un cambio de ventana en primer plano y el paquete
 *    está en la lista (cacheada) de apps protegidas -> decide si
 *    descuenta puntos y permite el acceso, o si debe mostrar el overlay
 *    de bloqueo.
 */
@AndroidEntryPoint
class GateAccessibilityService : AccessibilityService() {

    @Inject lateinit var recordAnkiAnswerUseCase: RecordAnkiAnswerUseCase
    @Inject lateinit var observeProtectedAppsUseCase: ObserveProtectedAppsUseCase
    @Inject lateinit var observePointsConfigUseCase: ObservePointsConfigUseCase
    @Inject lateinit var requestAppUnlockUseCase: RequestAppUnlockUseCase
    @Inject lateinit var overlayController: OverlayController

    private var serviceScope: CoroutineScope? = null

    private var lastForegroundPackage: String? = null
    private val protectedAppsCache = ConcurrentHashMap<String, ProtectedApp>()
    private val temporaryUnlocks = ConcurrentHashMap<String, Long>()

    @Volatile
    private var unlockSessionMillis: Long = PointsConfigDefaults.UNLOCK_SESSION_MINUTES * 60_000L

    override fun onServiceConnected() {
        super.onServiceConnected()
        // IMPORTANTE: usamos Main (no Default/IO) porque este scope es el
        // que dispara overlayController.show()/hide(), que manipulan
        // WindowManager y un ComposeView — operaciones de UI que SOLO se
        // pueden hacer desde el hilo principal. Las llamadas suspend a
        // Room siguen siendo seguras aquí: Room despacha internamente sus
        // propias consultas a su executor, sin bloquear este hilo.
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        serviceScope = scope

        scope.launch {
            observeProtectedAppsUseCase().collect { apps ->
                protectedAppsCache.clear()
                apps.filter { it.enabled }.forEach { protectedAppsCache[it.packageName] = it }
                Log.d(TAG, "Apps protegidas cacheadas: ${protectedAppsCache.keys}")
                DiagnosticsBus.onProtectedAppsUpdated(protectedAppsCache.keys.toList())
            }
        }

        scope.launch {
            observePointsConfigUseCase().collect { config ->
                unlockSessionMillis = config.unlockSessionMinutes * 60_000L
            }
        }

        Log.d(TAG, "GateAccessibilityService conectado.")
        DiagnosticsBus.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        // Log sin filtrar: si esto nunca aparece, el servicio no está
        // recibiendo eventos del sistema en absoluto (revisar que el
        // toggle de Accesibilidad siga activo y que el sistema no haya
        // matado el servicio).
        Log.d(TAG, "Evento crudo: pkg=$packageName tipo=${AccessibilityEvent.eventTypeToString(event.eventType)}")
        DiagnosticsBus.onEvent(packageName, AccessibilityEvent.eventTypeToString(event.eventType))

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED ->
                if (packageName == ANKIDROID_PACKAGE) handleAnkiClick(event)

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ->
                handleWindowStateChanged(packageName)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Servicio interrumpido por el sistema.")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope?.cancel()
        overlayController.hide()
    }

    // -----------------------------------------------------------------
    // 1. Registro de puntos por respuestas en AnkiDroid
    // -----------------------------------------------------------------
    private fun handleAnkiClick(event: AccessibilityEvent) {
        val source = event.source
        val button = source?.let { mapNodeToButton(it) } ?: mapNodeToButton(event)

        val rawDebugText = buildString {
            append("viewId=").append(source?.viewIdResourceName)
            append(" text=").append(source?.text ?: event.text?.joinToString(" "))
            append(" desc=").append(source?.contentDescription)
        }
        source?.recycle()

        Log.d(TAG, "Anki click crudo: $rawDebugText -> mapeado=$button")
        DiagnosticsBus.onAnkiRaw(rawDebugText, button?.name)

        if (button != null) {
            serviceScope?.launch { recordAnkiAnswerUseCase(button) }
        }
    }

    // -----------------------------------------------------------------
    // 2. Control de acceso a apps protegidas
    // -----------------------------------------------------------------
    private fun handleWindowStateChanged(packageName: String) {
        // Evita procesar cada cambio de pantalla DENTRO de la misma app
        // como si fuera una nueva "apertura".
        if (packageName == lastForegroundPackage) {
            DiagnosticsBus.onUnlockCheck("$packageName repetido, ignorado")
            return
        }

        // La propia ventana del overlay (al tomar foco) genera un evento
        // TYPE_WINDOW_STATE_CHANGED con nuestro propio paquete. Si lo
        // tratáramos como "el usuario navegó a FocusGate" y ocultáramos
        // el overlay, la app bloqueada volvería a quedar visible por un
        // instante, generaría OTRO evento, y el overlay se volvería a
        // mostrar: un parpadeo infinito. Por eso, mientras el overlay
        // esté activo, ignoramos por completo los eventos de nuestro
        // propio paquete (ni actualizamos lastForegroundPackage ni lo
        // ocultamos) — así seguimos "recordando" que la app bloqueada
        // sigue siendo la que está debajo.
        if (packageName == BuildConfig.APPLICATION_ID) {
            if (overlayController.isShowing()) {
                DiagnosticsBus.onUnlockCheck("Evento propio del overlay, ignorado")
                return
            }
            lastForegroundPackage = packageName
            overlayController.hide()
            return
        }

        lastForegroundPackage = packageName

        val protectedApp = protectedAppsCache[packageName]
        if (protectedApp == null) {
            DiagnosticsBus.onUnlockCheck("$packageName no está protegida (cache=${protectedAppsCache.keys})")
            overlayController.hide()
            return
        }

        val unlockedUntil = temporaryUnlocks[packageName]
        if (unlockedUntil != null && unlockedUntil > System.currentTimeMillis()) {
            // Todavía dentro de la ventana de gracia de un desbloqueo previo.
            DiagnosticsBus.onUnlockCheck("$packageName en sesión de gracia")
            overlayController.hide()
            return
        }

        DiagnosticsBus.onUnlockCheck("Evaluando $packageName...")
        serviceScope?.launch {
            val result = requestAppUnlockUseCase(packageName)

            // Entre que se disparó este chequeo (asíncrono) y que terminó,
            // pudo haber llegado un evento más reciente que ya cambió el
            // foco a otra app (típico en arranques "fríos" con splash
            // screen, que generan varios eventos de ventana seguidos).
            // Si eso pasó, esta evaluación quedó obsoleta: no debemos
            // mostrar un bloqueo para una app que ya no está en pantalla.
            if (packageName != lastForegroundPackage) {
                DiagnosticsBus.onUnlockCheck("Evaluación de $packageName descartada (obsoleta, ahora es $lastForegroundPackage)")
                return@launch
            }

            when (result) {
                is UnlockResult.Granted -> {
                    temporaryUnlocks[packageName] = System.currentTimeMillis() + unlockSessionMillis
                    overlayController.hide()
                    Log.d(TAG, "Acceso concedido a ${result.app.appLabel}, saldo restante=${result.remainingBalance}")
                    DiagnosticsBus.onUnlockCheck("Concedido: ${result.app.appLabel}, saldo=${result.remainingBalance}")
                }

                is UnlockResult.Denied -> {
                    Log.d(TAG, "Acceso denegado a ${result.app.appLabel}, faltan ${result.missing} puntos")
                    DiagnosticsBus.onUnlockCheck("Denegado: ${result.app.appLabel}, faltan ${result.missing}")
                    overlayController.show(
                        state = BlockedOverlayState(
                            appLabel = result.app.appLabel,
                            cost = result.app.costPoints,
                            balance = result.balance,
                            missing = result.missing
                        ),
                        onGoHome = {
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            overlayController.hide()
                        },
                        onOpenAnki = {
                            openAnkiDroid()
                            overlayController.hide()
                        }
                    )
                }

                UnlockResult.NotProtected -> overlayController.hide()
            }
        }
    }

    private fun openAnkiDroid() {
        val intent = packageManager.getLaunchIntentForPackage(ANKIDROID_PACKAGE)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.let { startActivity(it) }
    }

    // -----------------------------------------------------------------
    // Heurística de mapeo de nodo -> botón de Anki (misma idea que el POC)
    // -----------------------------------------------------------------
    private fun mapNodeToButton(node: AccessibilityNodeInfo): AnkiButton? {
        val viewId = node.viewIdResourceName ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        val combined = "$text $desc"

        return when {
            viewId.contains("ease1", true) || matchesAny(combined, AGAIN_WORDS) -> AnkiButton.AGAIN
            viewId.contains("ease2", true) || matchesAny(combined, HARD_WORDS) -> AnkiButton.HARD
            viewId.contains("ease3", true) || matchesAny(combined, GOOD_WORDS) -> AnkiButton.GOOD
            viewId.contains("ease4", true) || matchesAny(combined, EASY_WORDS) -> AnkiButton.EASY
            else -> null
        }
    }

    private fun mapNodeToButton(event: AccessibilityEvent): AnkiButton? {
        val combined = event.text?.joinToString(" ")?.lowercase() ?: return null
        return when {
            matchesAny(combined, AGAIN_WORDS) -> AnkiButton.AGAIN
            matchesAny(combined, HARD_WORDS) -> AnkiButton.HARD
            matchesAny(combined, GOOD_WORDS) -> AnkiButton.GOOD
            matchesAny(combined, EASY_WORDS) -> AnkiButton.EASY
            else -> null
        }
    }

    private fun matchesAny(text: String, words: List<String>): Boolean =
        words.any { text.startsWith(it) || text.contains(" $it") || text == it }

    private object PointsConfigDefaults {
        const val UNLOCK_SESSION_MINUTES = 2
    }

    companion object {
        private const val TAG = "GateAccessibilityService"
        private const val ANKIDROID_PACKAGE = "com.ichi2.anki"

        private val AGAIN_WORDS = listOf("again", "de nuevo", "otra vez")
        private val HARD_WORDS = listOf("hard", "difícil", "dificil")
        private val GOOD_WORDS = listOf("good", "bien")
        private val EASY_WORDS = listOf("easy", "fácil", "facil")
    }
}