package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.repository.ProtectedAppRepository
import javax.inject.Inject

class DeleteProtectedAppUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    suspend operator fun invoke(packageName: String) = protectedAppRepository.delete(packageName)
}
