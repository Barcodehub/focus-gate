package com.example.focusgate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.focusgate.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getById(taskId: Long): TaskEntity?

    @Insert
    suspend fun insert(entity: TaskEntity): Long

    /**
     * Solo actualiza la fila si `completed = 0`. El guard va en el propio
     * SQL (no en una lectura previa en Kotlin) para que sea atómico frente
     * a llamadas concurrentes: si dos completions llegan casi a la vez,
     * como máximo una de ellas afecta una fila. Devuelve las filas
     * afectadas (0 o 1).
     */
    @Query("UPDATE tasks SET completed = 1, lastCompletedAt = :timestamp WHERE id = :taskId AND completed = 0")
    suspend fun markCompleted(taskId: Long, timestamp: Long): Int

    @Query("UPDATE tasks SET completed = 0, lastResetAt = :timestamp WHERE id = :taskId")
    suspend fun reset(taskId: Long, timestamp: Long)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: Long)
}