package io.github.barcodehub.focusanki.domain.model

data class PointsTransaction(
    val id: Long,
    val timestamp: Long,
    val delta: Int,
    val reason: String,
    val source: TransactionSource
)
