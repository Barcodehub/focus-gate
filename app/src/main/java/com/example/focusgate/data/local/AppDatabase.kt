package com.example.focusgate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusgate.data.local.dao.PointsConfigDao
import com.example.focusgate.data.local.dao.PointsTransactionDao
import com.example.focusgate.data.local.dao.ProtectedAppDao
import com.example.focusgate.data.local.entity.PointsConfigEntity
import com.example.focusgate.data.local.entity.PointsTransactionEntity
import com.example.focusgate.data.local.entity.ProtectedAppEntity

@Database(
    entities = [
        PointsTransactionEntity::class,
        ProtectedAppEntity::class,
        PointsConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pointsTransactionDao(): PointsTransactionDao
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun pointsConfigDao(): PointsConfigDao
}
