package io.github.barcodehub.focusanki.domain.repository

import io.github.barcodehub.focusanki.domain.model.PointsConfig
import kotlinx.coroutines.flow.Flow

interface PointsConfigRepository {
    fun observeConfig(): Flow<PointsConfig>
    suspend fun currentConfig(): PointsConfig
    suspend fun updateConfig(config: PointsConfig)

    /** Crea la fila de configuración por defecto si todavía no existe. */
    suspend fun ensureDefaults()
}
