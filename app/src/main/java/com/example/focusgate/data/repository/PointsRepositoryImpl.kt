package com.example.focusgate.data.repository

import com.example.focusgate.data.local.dao.PointsTransactionDao
import com.example.focusgate.data.local.entity.PointsTransactionEntity
import com.example.focusgate.domain.model.PointsTransaction
import com.example.focusgate.domain.model.TransactionSource
import com.example.focusgate.domain.repository.PointsRepository
import com.example.focusgate.domain.repository.SpendOutcome
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
    source = TransactionSource.valueOf(source)
)
