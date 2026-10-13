package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.repository.TaskRepository
import io.github.barcodehub.focusanki.domain.validation.Limits
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /** Devuelve `false` si el título está vacío (no se crea nada). */
    suspend operator fun invoke(title: String, points: Int, description: String? = null): Boolean {
        val cleanTitle = title.trim().take(Limits.MAX_TITLE_LENGTH)
        if (cleanTitle.isEmpty()) return false
        taskRepository.createTask(
            title = cleanTitle,
            points = points.coerceIn(1, Limits.MAX_POINTS),
            description = description?.trim()?.take(Limits.MAX_DESCRIPTION_LENGTH)?.ifBlank { null }
        )
        return true
    }
}
