package io.github.barcodehub.focusanki.accessibility

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.barcodehub.focusanki.ui.overlay.BlockedOverlayScreen
import io.github.barcodehub.focusanki.ui.overlay.BlockedOverlayState
import io.github.barcodehub.focusanki.ui.theme.FocusAnkiTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsula todo el manejo de la ventana de overlay (WindowManager +
 * ComposeView + lifecycle propio). GateAccessibilityService solo conoce
 * `show`/`hide`; no sabe nada de WindowManager.LayoutParams ni de
 * lifecycle owners: esto es Single Responsibility + facilita testear o
 * reemplazar el mecanismo de bloqueo en el futuro (p. ej. por una
 * Activity transparente) sin tocar el servicio.
 */
@Singleton
class OverlayController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val overlayState = mutableStateOf<BlockedOverlayState?>(null)

    private val windowManager: WindowManager
        get() = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun show(state: BlockedOverlayState, onGoHome: () -> Unit, onOpenAnki: () -> Unit) {
        overlayState.value = state

        if (composeView != null) return // la ventana ya existe, solo se actualizó el estado

        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "No se puede mostrar el overlay: falta el permiso 'Dibujar sobre otras apps'.")
            return
        }

        val owner = OverlayLifecycleOwner().apply {
            performCreate()
            performStart()
            performResume()
        }
        lifecycleOwner = owner

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                FocusAnkiTheme {
                    overlayState.value?.let { currentState ->
                        BlockedOverlayScreen(
                            state = currentState,
                            onGoHome = onGoHome,
                            onOpenAnki = onOpenAnki
                        )
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        runCatching { windowManager.addView(view, params) }
            .onSuccess { composeView = view }
            .onFailure { Log.e(TAG, "No se pudo agregar el overlay", it) }
    }

    fun hide() {
        overlayState.value = null
        val view = composeView ?: return
        runCatching { windowManager.removeView(view) }
        lifecycleOwner?.performPause()
        lifecycleOwner?.performStop()
        lifecycleOwner?.performDestroy()
        composeView = null
        lifecycleOwner = null
    }

    fun isShowing(): Boolean = composeView != null

    companion object {
        private const val TAG = "OverlayController"
    }
}
