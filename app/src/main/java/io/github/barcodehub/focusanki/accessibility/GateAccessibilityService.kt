package io.github.barcodehub.focusanki.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.barcodehub.focusanki.BuildConfig
import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.model.UnlockResult
import io.github.barcodehub.focusanki.domain.usecase.ObservePointsConfigUseCase
import io.github.barcodehub.focusanki.domain.usecase.ObserveProtectedAppsUseCase
import io.github.barcodehub.focusanki.domain.usecase.RecordAnkiAnswerUseCase
import io.github.barcodehub.focusanki.domain.usecase.RequestAppUnlockUseCase
import io.github.barcodehub.focusanki.ui.overlay.BlockedOverlayState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Estado tocado solo desde el hilo principal (scope = Main.immediate).
    private val evaluating = HashSet<String>()
    private val recheckJobs = HashMap<String, Job>()
    private var lastAnkiAnswerAt = 0L

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
                dlog("Apps protegidas cacheadas: ${protectedAppsCache.keys}")
                DiagnosticsBus.onProtectedAppsUpdated(protectedAppsCache.keys.toList())
                // Si el usuario desactivó/eliminó la app que estaba bloqueada, el overlay sobra.
                val current = lastForegroundPackage
                if (overlayController.isShowing() && (current == null || !protectedAppsCache.containsKey(current))) {
                    overlayController.hide()
                }
            }
        }

        scope.launch {
            observePointsConfigUseCase().collect { config ->
                unlockSessionMillis = config.unlockSessionMinutes * 60_000L
            }
        }

        dlog("GateAccessibilityService conectado.")
        DiagnosticsBus.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        // Solo en builds de depuración: en release no se registra ni se
        // guarda qué apps usa el usuario (privacidad).
        if (BuildConfig.DEBUG) {
            val typeName = AccessibilityEvent.eventTypeToString(event.eventType)
            dlog("Evento crudo: pkg=$packageName tipo=$typeName")
            DiagnosticsBus.onEvent(packageName, typeName)
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED ->
                if (packageName == ANKIDROID_PACKAGE) handleAnkiClick(event)

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ->
                handleWindowStateChanged(packageName)
        }
    }

    override fun onInterrupt() {
        dlog("Servicio interrumpido por el sistema.")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope?.cancel()
        recheckJobs.clear()
        evaluating.clear()
        overlayController.hide()
    }

    // -----------------------------------------------------------------
    // 1. Registro de puntos por respuestas en AnkiDroid
    // -----------------------------------------------------------------
    private fun handleAnkiClick(event: AccessibilityEvent) {
        val source = event.source
        val button = if (source != null) {
            AnkiButtonMapper.map(source.viewIdResourceName, source.text, source.contentDescription)
        } else {
            // Sin nodo fuente no hay id de vista: solo se acepta texto exacto.
            AnkiButtonMapper.map(null, event.text.firstOrNull(), null)
        }

        if (BuildConfig.DEBUG) {
            val raw = "viewId=${source?.viewIdResourceName} text=${source?.text ?: event.text.joinToString(" ")} desc=${source?.contentDescription}"
            dlog("Anki click crudo: $raw -> mapeado=$button")
            DiagnosticsBus.onAnkiRaw(raw, button?.name)
        }

        if (button == null) return

        // Antirrebote: un mismo toque puede emitir eventos duplicados y no
        // queremos pagar dos veces por una sola respuesta.
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnkiAnswerAt < ANKI_DEBOUNCE_MS) return
        lastAnkiAnswerAt = now

        serviceScope?.launch { recordAnkiAnswerUseCase(button) }
    }

    // -----------------------------------------------------------------
    // 2. Control de acceso a apps protegidas
    // -----------------------------------------------------------------
    private fun handleWindowStateChanged(packageName: String) {
        // Ventanas "transitorias" (barra de notificaciones, teclado) se
        // superponen a la app de debajo SIN sacarla de primer plano y, al
        // cerrarse, la app de debajo no emite un nuevo evento. Si las
        // tratáramos como un cambio de app, bajar la barra de
        // notificaciones ocultaría el bloqueo y dejaría la app protegida
        // usable al subirla. Se ignoran por completo.
        if (isTransientWindow(packageName)) {
            DiagnosticsBus.onUnlockCheck("$packageName es transitoria, ignorada")
            return
        }

        // Evita procesar cada cambio de pantalla DENTRO de la misma app
        // como si fuera una nueva "apertura".
        if (packageName == lastForegroundPackage) {
            DiagnosticsBus.onUnlockCheck("$packageName repetido, ignorado")
            return
        }

        // La propia ventana del overlay (al tomar foco) genera un evento
        // con nuestro propio paquete. Mientras el overlay esté activo se
        // ignora para no caer en un parpadeo infinito.
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
        if (protectedApp == null || packageName == ANKIDROID_PACKAGE) {
            DiagnosticsBus.onUnlockCheck("$packageName no está protegida")
            overlayController.hide()
            return
        }

        val unlockedUntil = temporaryUnlocks[packageName]
        if (unlockedUntil != null && unlockedUntil > System.currentTimeMillis()) {
            DiagnosticsBus.onUnlockCheck("$packageName en sesión de gracia")
            overlayController.hide()
            return
        }

        evaluate(packageName)
    }

    private fun evaluate(packageName: String) {
        // Evita cobros dobles si llegan dos eventos del mismo paquete
        // mientras la primera evaluación sigue en curso.
        if (!evaluating.add(packageName)) return
        DiagnosticsBus.onUnlockCheck("Evaluando $packageName...")

        val scope = serviceScope
        if (scope == null) {
            evaluating.remove(packageName)
            return
        }

        scope.launch {
            try {
                val result = requestAppUnlockUseCase(packageName)

                // Evaluación obsoleta: el foco cambió mientras se resolvía.
                if (packageName != lastForegroundPackage) {
                    DiagnosticsBus.onUnlockCheck("Evaluación de $packageName descartada (obsoleta)")
                    return@launch
                }

                when (result) {
                    is UnlockResult.Granted -> {
                        temporaryUnlocks[packageName] = System.currentTimeMillis() + unlockSessionMillis
                        overlayController.hide()
                        scheduleRecheck(packageName)
                        dlog("Acceso concedido a ${result.app.appLabel}, saldo=${result.remainingBalance}")
                        DiagnosticsBus.onUnlockCheck("Concedido: ${result.app.appLabel}, saldo=${result.remainingBalance}")
                    }

                    is UnlockResult.Denied -> {
                        dlog("Acceso denegado a ${result.app.appLabel}, faltan ${result.missing}")
                        DiagnosticsBus.onUnlockCheck("Denegado: ${result.app.appLabel}, faltan ${result.missing}")
                        overlayController.show(
                            state = BlockedOverlayState(
                                appLabel = result.app.appLabel,
                                cost = result.app.costPoints,
                                balance = result.balance,
                                missing = result.missing
                            ),
                            onGoHome = ::goHomeAndDismiss,
                            onOpenAnki = ::openAnkiAndDismiss
                        )
                    }

                    UnlockResult.NotProtected -> overlayController.hide()
                }
            } finally {
                evaluating.remove(packageName)
            }
        }
    }

    /**
     * La "sesión" pagada dura [unlockSessionMillis]. Pasado ese tiempo, si
     * el usuario SIGUE en la app (sin generar eventos de ventana, p. ej.
     * haciendo scroll), se vuelve a evaluar: cobra de nuevo o bloquea. Sin
     * esto, pagar una vez daría uso ilimitado.
     */
    private fun scheduleRecheck(packageName: String) {
        val scope = serviceScope ?: return
        recheckJobs.remove(packageName)?.cancel()
        recheckJobs[packageName] = scope.launch {
            delay(unlockSessionMillis + RECHECK_MARGIN_MS)
            if (lastForegroundPackage == packageName && protectedAppsCache.containsKey(packageName)) {
                temporaryUnlocks.remove(packageName)
                evaluate(packageName)
            }
        }
    }

    private fun goHomeAndDismiss() {
        lastForegroundPackage = null // fuerza reevaluar al volver a la app protegida
        performGlobalAction(GLOBAL_ACTION_HOME)
        overlayController.hide()
    }

    private fun openAnkiAndDismiss() {
        lastForegroundPackage = null
        if (openAnkiDroid()) {
            overlayController.hide()
        } else {
            // Sin AnkiDroid NO se puede simplemente quitar el overlay: la
            // app protegida quedaría libre. Se manda al usuario al inicio.
            Toast.makeText(this, "AnkiDroid no está instalado", Toast.LENGTH_LONG).show()
            goHomeAndDismiss()
        }
    }

    private fun openAnkiDroid(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(ANKIDROID_PACKAGE) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { startActivity(intent) }.isSuccess
    }

    private fun isTransientWindow(packageName: String): Boolean {
        if (packageName == SYSTEM_UI_PACKAGE) return true
        val imm = getSystemService(InputMethodManager::class.java) ?: return false
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private object PointsConfigDefaults {
        const val UNLOCK_SESSION_MINUTES = 2
    }

    private fun dlog(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "GateAccessibilityService"
        private const val ANKIDROID_PACKAGE = "com.ichi2.anki"
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val ANKI_DEBOUNCE_MS = 400L
        private const val RECHECK_MARGIN_MS = 500L
    }
}
