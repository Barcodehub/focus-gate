package com.example.focusgate.domain.repository

import com.example.focusgate.domain.model.ProtectedApp
import kotlinx.coroutines.flow.Flow

interface ProtectedAppRepository {
    fun observeApps(): Flow<List<ProtectedApp>>
    suspend fun getByPackage(packageName: String): ProtectedApp?
    suspend fun upsert(app: ProtectedApp)
    suspend fun delete(packageName: String)
}
