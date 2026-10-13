package io.github.barcodehub.focusanki.data.repository

import io.github.barcodehub.focusanki.data.local.dao.PointsConfigDao
import io.github.barcodehub.focusanki.data.local.entity.PointsConfigEntity
import io.github.barcodehub.focusanki.domain.model.PointsConfig
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PointsConfigRepositoryImpl @Inject constructor(
    private val dao: PointsConfigDao
) : PointsConfigRepository {

    override fun observeConfig(): Flow<PointsConfig> =
        dao.observe().map { it?.toDomain() ?: PointsConfig.DEFAULT }

    override suspend fun currentConfig(): PointsConfig =
        dao.getOnce()?.toDomain() ?: PointsConfig.DEFAULT

    override suspend fun updateConfig(config: PointsConfig) = dao.upsert(config.toEntity())

    override suspend fun ensureDefaults() {
        if (dao.getOnce() == null) {
            dao.upsert(PointsConfig.DEFAULT.toEntity())
        }
    }
}

private fun PointsConfigEntity.toDomain() = PointsConfig(again, hard, good, easy, unlockSessionMinutes)
private fun PointsConfig.toEntity() = PointsConfigEntity(
    id = 0,
    again = again,
    hard = hard,
    good = good,
    easy = easy,
    unlockSessionMinutes = unlockSessionMinutes
)
