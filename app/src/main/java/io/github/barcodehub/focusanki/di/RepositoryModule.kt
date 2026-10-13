package io.github.barcodehub.focusanki.di

import io.github.barcodehub.focusanki.data.repository.PointsConfigRepositoryImpl
import io.github.barcodehub.focusanki.data.repository.PointsRepositoryImpl
import io.github.barcodehub.focusanki.data.repository.ProtectedAppRepositoryImpl
import io.github.barcodehub.focusanki.data.repository.TaskRepositoryImpl
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import io.github.barcodehub.focusanki.domain.repository.PointsRepository
import io.github.barcodehub.focusanki.domain.repository.ProtectedAppRepository
import io.github.barcodehub.focusanki.domain.repository.TaskRepository
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

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
