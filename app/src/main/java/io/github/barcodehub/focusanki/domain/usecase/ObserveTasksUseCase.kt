package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.Task
import io.github.barcodehub.focusanki.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeTasks()
}