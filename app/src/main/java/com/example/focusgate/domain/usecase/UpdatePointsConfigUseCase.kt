package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.PointsConfig
import com.example.focusgate.domain.repository.PointsConfigRepository
import javax.inject.Inject

class UpdatePointsConfigUseCase @Inject constructor(
    private val pointsConfigRepository: PointsConfigRepository
) {
    suspend operator fun invoke(config: PointsConfig) = pointsConfigRepository.updateConfig(config)
}
