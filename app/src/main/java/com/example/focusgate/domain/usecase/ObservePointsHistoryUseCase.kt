package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.PointsTransaction
import com.example.focusgate.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsHistoryUseCase @Inject constructor(
    private val pointsRepository: PointsRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<PointsTransaction>> =
        pointsRepository.observeRecentTransactions(limit)
}
