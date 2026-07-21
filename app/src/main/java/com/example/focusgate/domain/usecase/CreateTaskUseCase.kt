package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(title: String, points: Int, description: String? = null) {
        taskRepository.createTask(title = title, points = points, description = description)
    }
}