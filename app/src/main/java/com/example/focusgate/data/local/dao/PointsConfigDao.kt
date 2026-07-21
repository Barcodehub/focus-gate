package com.example.focusgate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focusgate.data.local.entity.PointsConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsConfigDao {

    @Query("SELECT * FROM points_config WHERE id = 0")
    fun observe(): Flow<PointsConfigEntity?>

    @Query("SELECT * FROM points_config WHERE id = 0")
    suspend fun getOnce(): PointsConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PointsConfigEntity)
}
