package com.example.focusgate.domain.repository

import com.example.focusgate.domain.model.PointsTransaction
import com.example.focusgate.domain.model.TransactionSource
import kotlinx.coroutines.flow.Flow

/** Resultado de intentar gastar puntos de forma atómica. */
data class SpendOutcome(val success: Boolean, val balanceAfter: Int)

interface PointsRepository {
    fun observeBalance(): Flow<Int>
    fun observeRecentTransactions(limit: Int = 50): Flow<List<PointsTransaction>>
    suspend fun currentBalance(): Int
    suspend fun addPoints(delta: Int, reason: String, source: TransactionSource)

    /**
     * Comprueba el saldo y descuenta [cost] en una única operación atómica
     * (implementada como una transacción de Room), evitando condiciones de
     * carrera entre "leer saldo" y "descontar".
     */
    suspend fun trySpend(cost: Int, reason: String): SpendOutcome
}
