package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.PointsConfig
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import io.github.barcodehub.focusanki.domain.validation.Limits
import javax.inject.Inject

class UpdatePointsConfigUseCase @Inject constructor(
    private val pointsConfigRepository: PointsConfigRepository
) {
    suspend operator fun invoke(config: PointsConfig) = pointsConfigRepository.updateConfig(
        PointsConfig(
            again = config.again.coerceIn(0, Limits.MAX_ANKI_POINTS),
            hard = config.hard.coerceIn(0, Limits.MAX_ANKI_POINTS),
            good = config.good.coerceIn(0, Limits.MAX_ANKI_POINTS),
            easy = config.easy.coerceIn(0, Limits.MAX_ANKI_POINTS),
            unlockSessionMinutes = config.unlockSessionMinutes
                .coerceIn(Limits.MIN_UNLOCK_MINUTES, Limits.MAX_UNLOCK_MINUTES)
        )
    )
}
