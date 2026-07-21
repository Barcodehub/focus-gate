package com.example.focusgate.di

import android.content.Context
import androidx.room.Room
import com.example.focusgate.data.local.AppDatabase
import com.example.focusgate.data.local.dao.PointsConfigDao
import com.example.focusgate.data.local.dao.PointsTransactionDao
import com.example.focusgate.data.local.dao.ProtectedAppDao
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
        Room.databaseBuilder(context, AppDatabase::class.java, "focusgate.db").build()

    @Provides
    fun providePointsTransactionDao(db: AppDatabase): PointsTransactionDao = db.pointsTransactionDao()

    @Provides
    fun provideProtectedAppDao(db: AppDatabase): ProtectedAppDao = db.protectedAppDao()

    @Provides
    fun providePointsConfigDao(db: AppDatabase): PointsConfigDao = db.pointsConfigDao()
}
