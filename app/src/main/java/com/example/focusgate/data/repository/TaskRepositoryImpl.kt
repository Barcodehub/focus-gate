package com.example.focusgate.data.repository

import androidx.room.withTransaction
import com.example.focusgate.data.local.AppDatabase
import com.example.focusgate.data.local.dao.PointsTransactionDao
import com.example.focusgate.data.local.dao.TaskDao
import com.example.focusgate.data.local.entity.PointsTransactionEntity
import com.example.focusgate.data.local.entity.TaskEntity
import com.example.focusgate.domain.model.Task
import com.example.focusgate.domain.model.TransactionSource
import com.example.focusgate.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val taskDao: TaskDao,
    private val pointsTransactionDao: PointsTransactionDao
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun createTask(title: String, points: Int, description: String?) {
        taskDao.insert(
            TaskEntity(
                title = title,
                description = description,
                points = points,
                completed = false,
                createdAt = System.currentTimeMillis(),
                lastCompletedAt = null,
                lastResetAt = null
            )
        )
    }

    override suspend fun completeTask(taskId: Long): Boolean = appDatabase.withTransaction {
        val task = taskDao.getById(taskId) ?: return@withTransaction false
        val rowsUpdated = taskDao.markCompleted(taskId, System.currentTimeMillis())
        if (rowsUpdated > 0) {
            pointsTransactionDao.insert(
                PointsTransactionEntity(
                    timestamp = System.currentTimeMillis(),
                    delta = task.points,
                    reason = "Tarea: ${task.title}",
                    source = TransactionSource.TASK_COMPLETED.name
                )
            )
            true
        } else {
            false
        }
    }

    override suspend fun resetTask(taskId: Long) {
        taskDao.reset(taskId, System.currentTimeMillis())
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
    }
}

private fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    points = points,
    completed = completed,
    createdAt = createdAt,
    lastCompletedAt = lastCompletedAt,
    lastResetAt = lastResetAt
)