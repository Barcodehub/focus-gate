package com.example.focusgate.domain.model

/** Resultado de intentar abrir una app protegida. */
sealed interface UnlockResult {
    data class Granted(val app: ProtectedApp, val remainingBalance: Int) : UnlockResult
    data class Denied(val app: ProtectedApp, val balance: Int, val missing: Int) : UnlockResult
    data object NotProtected : UnlockResult
}
