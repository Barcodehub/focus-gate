package com.example.focusgate.domain.usecase

import com.example.focusgate.domain.model.UnlockResult
import com.example.focusgate.domain.repository.PointsRepository
import com.example.focusgate.domain.repository.ProtectedAppRepository
import javax.inject.Inject

/**
 * Decide si se puede abrir una app protegida: si hay saldo suficiente lo
 * descuenta (de forma atómica) y concede acceso; si no, informa cuánto
 * falta para poder mostrar la pantalla de bloqueo.
 */
class RequestAppUnlockUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository,
    private val pointsRepository: PointsRepository
) {
    suspend operator fun invoke(packageName: String): UnlockResult {
        val app = protectedAppRepository.getByPackage(packageName)?.takeIf { it.enabled }
            ?: return UnlockResult.NotProtected

        val outcome = pointsRepository.trySpend(
            cost = app.costPoints,
            reason = "Desbloqueo: ${app.appLabel}"
        )

        return if (outcome.success) {
            UnlockResult.Granted(app, outcome.balanceAfter)
        } else {
            UnlockResult.Denied(app, outcome.balanceAfter, app.costPoints - outcome.balanceAfter)
        }
    }
}
