package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.PointsConfig
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsConfigUseCase @Inject constructor(
    private val pointsConfigRepository: PointsConfigRepository
) {
    operator fun invoke(): Flow<PointsConfig> = pointsConfigRepository.observeConfig()
}
