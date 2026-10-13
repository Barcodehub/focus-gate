package io.github.barcodehub.focusanki.data.repository

import io.github.barcodehub.focusanki.data.local.dao.PointsTransactionDao
import io.github.barcodehub.focusanki.data.local.entity.PointsTransactionEntity
import io.github.barcodehub.focusanki.domain.model.PointsTransaction
import io.github.barcodehub.focusanki.domain.model.TransactionSource
import io.github.barcodehub.focusanki.domain.repository.PointsRepository
import io.github.barcodehub.focusanki.domain.repository.SpendOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PointsRepositoryImpl @Inject constructor(
    private val dao: PointsTransactionDao
) : PointsRepository {

    override fun observeBalance(): Flow<Int> = dao.observeBalance()

    override fun observeRecentTransactions(limit: Int): Flow<List<PointsTransaction>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun currentBalance(): Int = dao.getBalanceOnce()

    override suspend fun addPoints(delta: Int, reason: String, source: TransactionSource) {
        dao.insert(
            PointsTransactionEntity(
                timestamp = System.currentTimeMillis(),
                delta = delta,
                reason = reason,
                source = source.name
            )
        )
    }

    override suspend fun trySpend(cost: Int, reason: String): SpendOutcome {
        val outcome = dao.trySpend(cost, reason, TransactionSource.APP_UNLOCK.name)
        return SpendOutcome(outcome.success, outcome.balanceAfter)
    }
}

private fun PointsTransactionEntity.toDomain() = PointsTransaction(
    id = id,
    timestamp = timestamp,
    delta = delta,
    reason = reason,
    // Una fila con un 'source' desconocido (p. ej. tras un downgrade) no debe tumbar el historial.
    source = runCatching { TransactionSource.valueOf(source) }.getOrDefault(TransactionSource.MANUAL_ADJUSTMENT)
)
