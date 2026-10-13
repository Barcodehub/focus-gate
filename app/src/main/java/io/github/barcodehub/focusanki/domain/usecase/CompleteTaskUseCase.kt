package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Boolean = taskRepository.completeTask(taskId)
}