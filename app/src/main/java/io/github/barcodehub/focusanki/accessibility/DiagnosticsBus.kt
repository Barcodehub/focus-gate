package io.github.barcodehub.focusanki.accessibility

import io.github.barcodehub.focusanki.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Canal de diagnóstico en memoria (mismo proceso) para poder ver, desde la
 * UI de la app, qué está haciendo realmente el AccessibilityService, sin
 * depender de que el usuario sepa filtrar Logcat.
 */
object DiagnosticsBus {

    data class State(
        val serviceConnected: Boolean = false,
        val serviceConnectedAt: Long? = null,
        val totalEventsSeen: Long = 0,
        val lastEventPackage: String? = null,
        val lastEventType: String? = null,
        val lastEventAt: Long? = null,
        val protectedAppsCached: Int = 0,
        val protectedPackagesCached: List<String> = emptyList(),
        val lastAnkiRawText: String? = null,
        val lastAnkiMappedButton: String? = null,
        val lastUnlockCheck: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun onServiceConnected() {
        if (!BuildConfig.DEBUG) return
        _state.update { it.copy(serviceConnected = true, serviceConnectedAt = System.currentTimeMillis()) }
    }

    fun onEvent(packageName: String, type: String) {
        if (!BuildConfig.DEBUG) return
        _state.update {
            it.copy(
                lastEventPackage = packageName,
                lastEventType = type,
                lastEventAt = System.currentTimeMillis(),
                totalEventsSeen = it.totalEventsSeen + 1
            )
        }
    }

    fun onProtectedAppsUpdated(packages: List<String>) {
        if (!BuildConfig.DEBUG) return
        _state.update { it.copy(protectedAppsCached = packages.size, protectedPackagesCached = packages) }
    }

    fun onAnkiRaw(rawText: String, mappedButton: String?) {
        if (!BuildConfig.DEBUG) return
        _state.update { it.copy(lastAnkiRawText = rawText, lastAnkiMappedButton = mappedButton ?: "sin mapear") }
    }

    fun onUnlockCheck(message: String) {
        if (!BuildConfig.DEBUG) return
        _state.update { it.copy(lastUnlockCheck = message) }
    }
}
