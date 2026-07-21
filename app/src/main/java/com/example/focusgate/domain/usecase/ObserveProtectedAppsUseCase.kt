package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.ProtectedApp
import com.example.focusgate.domain.repository.ProtectedAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProtectedAppsUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    operator fun invoke(): Flow<List<ProtectedApp>> = protectedAppRepository.observeApps()
}
