package com.example.focusgate.di

import com.example.focusgate.data.repository.PointsConfigRepositoryImpl
import com.example.focusgate.data.repository.PointsRepositoryImpl
import com.example.focusgate.data.repository.ProtectedAppRepositoryImpl
import com.example.focusgate.domain.repository.PointsConfigRepository
import com.example.focusgate.domain.repository.PointsRepository
import com.example.focusgate.domain.repository.ProtectedAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPointsRepository(impl: PointsRepositoryImpl): PointsRepository

    @Binds
    @Singleton
    abstract fun bindProtectedAppRepository(impl: ProtectedAppRepositoryImpl): ProtectedAppRepository

    @Binds
    @Singleton
    abstract fun bindPointsConfigRepository(impl: PointsConfigRepositoryImpl): PointsConfigRepository
}
