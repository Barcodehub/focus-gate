package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) = taskRepository.deleteTask(taskId)
}