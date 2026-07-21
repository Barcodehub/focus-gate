package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsBalanceUseCase @Inject constructor(
    private val pointsRepository: PointsRepository
) {
    operator fun invoke(): Flow<Int> = pointsRepository.observeBalance()
}
