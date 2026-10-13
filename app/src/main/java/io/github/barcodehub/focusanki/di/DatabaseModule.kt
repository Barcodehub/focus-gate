package io.github.barcodehub.focusanki.di

import android.content.Context
import androidx.room.Room
import io.github.barcodehub.focusanki.data.local.AppDatabase
import io.github.barcodehub.focusanki.data.local.MIGRATION_1_2
import io.github.barcodehub.focusanki.data.local.dao.PointsConfigDao
import io.github.barcodehub.focusanki.data.local.dao.PointsTransactionDao
import io.github.barcodehub.focusanki.data.local.dao.ProtectedAppDao
import io.github.barcodehub.focusanki.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "focusanki.db")
            .addMigrations(MIGRATION_1_2)
            .build()


    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun providePointsTransactionDao(db: AppDatabase): PointsTransactionDao = db.pointsTransactionDao()

    @Provides
    fun provideProtectedAppDao(db: AppDatabase): ProtectedAppDao = db.protectedAppDao()

    @Provides
    fun providePointsConfigDao(db: AppDatabase): PointsConfigDao = db.pointsConfigDao()
}
