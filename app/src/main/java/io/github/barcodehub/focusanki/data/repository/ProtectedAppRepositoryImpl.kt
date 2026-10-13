package io.github.barcodehub.focusanki.data.repository

import io.github.barcodehub.focusanki.data.local.dao.ProtectedAppDao
import io.github.barcodehub.focusanki.data.local.entity.ProtectedAppEntity
import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.repository.ProtectedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProtectedAppRepositoryImpl @Inject constructor(
    private val dao: ProtectedAppDao
) : ProtectedAppRepository {

    override fun observeApps(): Flow<List<ProtectedApp>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByPackage(packageName: String): ProtectedApp? =
        dao.getByPackage(packageName)?.toDomain()

    override suspend fun upsert(app: ProtectedApp) = dao.upsert(app.toEntity())

    override suspend fun delete(packageName: String) = dao.delete(packageName)
}

private fun ProtectedAppEntity.toDomain() = ProtectedApp(packageName, appLabel, costPoints, enabled)
private fun ProtectedApp.toEntity() = ProtectedAppEntity(packageName, appLabel, costPoints, enabled)
