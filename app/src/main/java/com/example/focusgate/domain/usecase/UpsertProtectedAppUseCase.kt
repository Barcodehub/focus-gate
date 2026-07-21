package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.ProtectedApp
import com.example.focusgate.domain.repository.ProtectedAppRepository
import javax.inject.Inject

class UpsertProtectedAppUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    suspend operator fun invoke(app: ProtectedApp) = protectedAppRepository.upsert(app)
}
