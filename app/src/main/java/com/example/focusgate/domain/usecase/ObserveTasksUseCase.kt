package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.Task
import com.example.focusgate.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeTasks()
}