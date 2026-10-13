package io.github.barcodehub.focusanki.domain.usecase

import io.github.barcodehub.focusanki.BuildConfig
import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.repository.ProtectedAppRepository
import io.github.barcodehub.focusanki.domain.validation.Limits
import javax.inject.Inject

class UpsertProtectedAppUseCase @Inject constructor(
    private val protectedAppRepository: ProtectedAppRepository
) {
    /**
     * Normaliza el costo (un costo negativo regalaría puntos al "gastar")
     * y rechaza apps que nunca deben bloquearse: AnkiDroid (se perdería la
     * única forma de ganar puntos) y la propia app.
     */
    suspend operator fun invoke(app: ProtectedApp): Boolean {
        if (app.packageName in NEVER_PROTECTED) return false
        protectedAppRepository.upsert(
            app.copy(costPoints = app.costPoints.coerceIn(Limits.MIN_COST, Limits.MAX_POINTS))
        )
        return true
    }

    companion object {
        val NEVER_PROTECTED = setOf(BuildConfig.APPLICATION_ID, "com.ichi2.anki")
    }
}
