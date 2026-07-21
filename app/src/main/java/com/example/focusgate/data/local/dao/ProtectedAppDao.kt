package com.example.focusgate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focusgate.data.local.entity.ProtectedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedAppDao {

    @Query("SELECT * FROM protected_apps ORDER BY appLabel")
    fun observeAll(): Flow<List<ProtectedAppEntity>>

    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): ProtectedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProtectedAppEntity)

    @Query("DELETE FROM protected_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
