package io.github.barcodehub.focusanki.domain.model

import java.time.Instant
import java.time.ZoneId

data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val points: Int,
    val completed: Boolean,
    val createdAt: Long,
    val lastCompletedAt: Long?,
    val lastResetAt: Long?
) {

    /**
     * Una tarea completada solo se puede reiniciar a partir del día
     * siguiente al que se completó. Sin esta regla, "completar -> reiniciar
     * -> completar" generaría puntos ilimitados.
     */
    fun canReset(nowMillis: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): Boolean {
        if (!completed) return false
        val completedAt = lastCompletedAt ?: return true
        val startOfToday = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
        return completedAt < startOfToday
    }
}
