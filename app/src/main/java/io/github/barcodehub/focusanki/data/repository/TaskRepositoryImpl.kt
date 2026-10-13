package io.github.barcodehub.focusanki.data.repository

import androidx.room.withTransaction
import io.github.barcodehub.focusanki.data.local.AppDatabase
import io.github.barcodehub.focusanki.data.local.dao.PointsTransactionDao
import io.github.barcodehub.focusanki.data.local.dao.TaskDao
import io.github.barcodehub.focusanki.data.local.entity.PointsTransactionEntity
import io.github.barcodehub.focusanki.data.local.entity.TaskEntity
import io.github.barcodehub.focusanki.domain.model.Task
import io.github.barcodehub.focusanki.domain.model.TransactionSource
import io.github.barcodehub.focusanki.domain.repository.TaskRepository
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

    override suspend fun resetTask(taskId: Long): Boolean {
        val task = taskDao.getById(taskId)?.toDomain() ?: return false
        if (!task.canReset()) return false
        taskDao.reset(taskId, System.currentTimeMillis())
        return true
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