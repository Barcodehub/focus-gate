package io.github.barcodehub.focusanki

import io.github.barcodehub.focusanki.domain.model.Task
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TaskResetTest {
    private val zone = ZoneId.of("America/Bogota")
    private fun at(day: Int, hour: Int) = ZonedDateTime.of(2026, 10, day, hour, 0, 0, 0, zone).toInstant().toEpochMilli()

    private fun task(completed: Boolean, completedAt: Long?) = Task(
        id = 1, title = "t", description = null, points = 5, completed = completed,
        createdAt = 0, lastCompletedAt = completedAt, lastResetAt = null
    )

    @Test fun `no se puede reiniciar el mismo dia`() {
        assertFalse(task(true, at(13, 8)).canReset(at(13, 23), zone))
    }

    @Test fun `se puede reiniciar al dia siguiente, incluso pasada la medianoche`() {
        assertTrue(task(true, at(13, 23)).canReset(at(14, 0), zone))
    }

    @Test fun `una tarea pendiente no se reinicia`() {
        assertFalse(task(false, null).canReset(at(14, 9), zone))
    }
}
