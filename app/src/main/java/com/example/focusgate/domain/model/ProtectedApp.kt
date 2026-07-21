package com.example.focusgate.domain.model

data class ProtectedApp(
    val packageName: String,
    val appLabel: String,
    val costPoints: Int,
    val enabled: Boolean
)
