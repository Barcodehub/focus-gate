package io.github.barcodehub.focusanki.domain.repository

import io.github.barcodehub.focusanki.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun createTask(title: String, points: Int, description: String?)

    /**
     * Marca la tarea como completada y registra la transacción de puntos,
     * de forma atómica. Devuelve `false` sin efecto alguno si la tarea ya
     * estaba completada (evita duplicar puntos ante doble-tap o eventos
     * repetidos).
     */
    suspend fun completeTask(taskId: Long): Boolean

    /** `false` si la tarea no existe o todavía no se puede reiniciar (ver `Task.canReset`). */
    suspend fun resetTask(taskId: Long): Boolean
    suspend fun deleteTask(taskId: Long)
}