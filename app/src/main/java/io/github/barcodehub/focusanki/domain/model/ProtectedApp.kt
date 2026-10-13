package io.github.barcodehub.focusanki.domain.model

data class ProtectedApp(
    val packageName: String,
    val appLabel: String,
    val costPoints: Int,
    val enabled: Boolean
)
