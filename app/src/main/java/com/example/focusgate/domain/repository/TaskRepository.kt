package com.example.focusgate.domain.repository

import com.example.focusgate.domain.model.Task
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

    suspend fun resetTask(taskId: Long)
    suspend fun deleteTask(taskId: Long)
}