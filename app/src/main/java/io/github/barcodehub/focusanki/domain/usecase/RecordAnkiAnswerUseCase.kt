package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.AnkiButton
import io.github.barcodehub.focusanki.domain.model.TransactionSource
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import io.github.barcodehub.focusanki.domain.repository.PointsRepository
import javax.inject.Inject

/** Convierte una respuesta de AnkiDroid en puntos, según la configuración actual. */
class RecordAnkiAnswerUseCase @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val pointsConfigRepository: PointsConfigRepository
) {
    suspend operator fun invoke(button: AnkiButton) {
        val config = pointsConfigRepository.currentConfig()
        val delta = config.pointsFor(button)
        if (delta == 0) return // 'Again' = 0: no ensuciar el historial con filas vacías
        pointsRepository.addPoints(
            delta = delta,
            reason = "Anki: ${button.name}",
            source = TransactionSource.ANKI_ANSWER
        )
    }
}
