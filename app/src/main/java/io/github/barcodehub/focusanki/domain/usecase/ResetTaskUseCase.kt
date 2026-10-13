package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.repository.TaskRepository
import javax.inject.Inject

class ResetTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /** `false` si la tarea no se puede reiniciar todavía (ver [io.github.barcodehub.focusanki.domain.model.Task.canReset]). */
    suspend operator fun invoke(taskId: Long): Boolean = taskRepository.resetTask(taskId)
}
