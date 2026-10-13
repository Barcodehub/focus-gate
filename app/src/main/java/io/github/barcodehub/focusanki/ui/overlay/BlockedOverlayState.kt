package io.github.barcodehub.focusanki.ui.overlay

data class BlockedOverlayState(
    val appLabel: String,
    val cost: Int,
    val balance: Int,
    val missing: Int
)
