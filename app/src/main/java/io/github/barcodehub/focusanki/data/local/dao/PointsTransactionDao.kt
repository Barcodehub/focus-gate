package io.github.barcodehub.focusanki.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.github.barcodehub.focusanki.data.local.entity.PointsTransactionEntity
import kotlinx.coroutines.flow.Flow

/** Resultado interno (capa de datos) de un intento de gasto atómico. */
data class SpendOutcomeEntity(val success: Boolean, val balanceAfter: Int)

/**
 * Se declara como clase abstracta (en vez de interface) porque necesita un
 * método con cuerpo (`trySpend`) anotado con @Transaction que combina dos
 * operaciones (leer saldo + insertar) en una sola unidad atómica.
 */
@Dao
abstract class PointsTransactionDao {

    @Insert
    abstract suspend fun insert(entity: PointsTransactionEntity)

    @Query("SELECT COALESCE(SUM(delta), 0) FROM points_transactions")
    abstract suspend fun getBalanceOnce(): Int

    @Query("SELECT COALESCE(SUM(delta), 0) FROM points_transactions")
    abstract fun observeBalance(): Flow<Int>

    @Query("SELECT * FROM points_transactions ORDER BY timestamp DESC LIMIT :limit")
    abstract fun observeRecent(limit: Int): Flow<List<PointsTransactionEntity>>

    /**
     * Lee el saldo y, si alcanza, inserta el movimiento negativo; todo
     * dentro de la misma transacción de Room para que sea atómico frente a
     * escrituras concurrentes (p. ej. una respuesta de Anki llegando al
     * mismo tiempo que se intenta abrir una app protegida).
     */
    @Transaction
    open suspend fun trySpend(cost: Int, reason: String, source: String): SpendOutcomeEntity {
        val balance = getBalanceOnce()
        return if (balance >= cost) {
            insert(
                PointsTransactionEntity(
                    timestamp = System.currentTimeMillis(),
                    delta = -cost,
                    reason = reason,
                    source = source
                )
            )
            SpendOutcomeEntity(success = true, balanceAfter = balance - cost)
        } else {
            SpendOutcomeEntity(success = false, balanceAfter = balance)
        }
    }
}
