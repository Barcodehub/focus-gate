package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.repository.ProtectedAppRepository
import javax.inject.Inject

class DeleteProtectedAppUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    suspend operator fun invoke(packageName: String) = protectedAppRepository.delete(packageName)
}
