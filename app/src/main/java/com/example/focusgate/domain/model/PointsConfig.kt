package com.example.focusgate.domain.model

/**
 * Puntos otorgados por cada botón de respuesta de AnkiDroid, y minutos de
 * "sesión de desbloqueo": tiempo de gracia tras pagar por una app en el
 * que se puede volver a abrir sin que se cobre de nuevo (evita cobros
 * repetidos por cambiar de pantalla o volver un segundo por una
 * notificación).
 */
data class PointsConfig(
    val again: Int,
    val hard: Int,
    val good: Int,
    val easy: Int,
    val unlockSessionMinutes: Int
) {
    companion object {
        val DEFAULT = PointsConfig(
            again = 0,
            hard = 2,
            good = 5,
            easy = 8,
            unlockSessionMinutes = 2
        )
    }

    fun pointsFor(button: AnkiButton): Int = when (button) {
        AnkiButton.AGAIN -> again
        AnkiButton.HARD -> hard
        AnkiButton.GOOD -> good
        AnkiButton.EASY -> easy
    }
}
