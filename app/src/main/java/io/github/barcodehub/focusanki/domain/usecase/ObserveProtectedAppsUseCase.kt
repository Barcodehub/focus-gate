package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.repository.ProtectedAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProtectedAppsUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    operator fun invoke(): Flow<List<ProtectedApp>> = protectedAppRepository.observeApps()
}
