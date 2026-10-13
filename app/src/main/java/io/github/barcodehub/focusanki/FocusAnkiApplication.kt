package io.github.barcodehub.focusanki

import android.app.Application
import io.github.barcodehub.focusanki.domain.repository.PointsConfigRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FocusAnkiApplication : Application() {

    @Inject
    lateinit var pointsConfigRepository: PointsConfigRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Garantiza que exista una fila de configuración de puntos desde
        // el primer arranque, para que el resto de la app pueda asumir
        // que PointsConfigRepository.currentConfig() nunca es un valor
        // "inventado" en tiempo de lectura.
        applicationScope.launch {
            pointsConfigRepository.ensureDefaults()
        }
    }
}
