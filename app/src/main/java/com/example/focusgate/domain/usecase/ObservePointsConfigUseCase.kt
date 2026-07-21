package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.PointsConfig
import com.example.focusgate.domain.repository.PointsConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsConfigUseCase @Inject constructor(
    private val pointsConfigRepository: PointsConfigRepository
) {
    operator fun invoke(): Flow<PointsConfig> = pointsConfigRepository.observeConfig()
}
