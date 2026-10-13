package io.github.barcodehub.focusanki.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.barcodehub.focusanki.data.local.dao.PointsConfigDao
import io.github.barcodehub.focusanki.data.local.dao.PointsTransactionDao
import io.github.barcodehub.focusanki.data.local.dao.ProtectedAppDao
import io.github.barcodehub.focusanki.data.local.entity.PointsConfigEntity
import io.github.barcodehub.focusanki.data.local.entity.PointsTransactionEntity
import io.github.barcodehub.focusanki.data.local.entity.ProtectedAppEntity

@Database(
    entities = [
        PointsTransactionEntity::class,
        ProtectedAppEntity::class,
        PointsConfigEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pointsTransactionDao(): PointsTransactionDao
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun pointsConfigDao(): PointsConfigDao
}