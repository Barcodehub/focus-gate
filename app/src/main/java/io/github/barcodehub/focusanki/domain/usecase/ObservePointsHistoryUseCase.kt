package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.PointsTransaction
import io.github.barcodehub.focusanki.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsHistoryUseCase @Inject constructor(
    private val pointsRepository: PointsRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<PointsTransaction>> =
        pointsRepository.observeRecentTransactions(limit)
}
