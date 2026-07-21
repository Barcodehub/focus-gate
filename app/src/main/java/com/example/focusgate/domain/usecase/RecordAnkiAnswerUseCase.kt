package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.AnkiButton
import com.example.focusgate.domain.model.TransactionSource
import com.example.focusgate.domain.repository.PointsConfigRepository
import com.example.focusgate.domain.repository.PointsRepository
import javax.inject.Inject

/** Convierte una respuesta de AnkiDroid en puntos, según la configuración actual. */
class RecordAnkiAnswerUseCase @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val pointsConfigRepository: PointsConfigRepository
) {
    suspend operator fun invoke(button: AnkiButton) {
        val config = pointsConfigRepository.currentConfig()
        val delta = config.pointsFor(button)
        pointsRepository.addPoints(
            delta = delta,
            reason = "Anki: ${button.name}",
            source = TransactionSource.ANKI_ANSWER
        )
    }
}
