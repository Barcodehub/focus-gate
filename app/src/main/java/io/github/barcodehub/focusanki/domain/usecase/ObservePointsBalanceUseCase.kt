package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePointsBalanceUseCase @Inject constructor(
    private val pointsRepository: PointsRepository
) {
    operator fun invoke(): Flow<Int> = pointsRepository.observeBalance()
}
