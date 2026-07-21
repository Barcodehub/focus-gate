package com.example.focusgate.domain.model

/**
 * Origen de un movimiento de puntos. Se guarda tal cual en la base de
 * datos para que, en el futuro, Estadísticas/Rachas/Logros puedan
 * consultar el historial sin necesidad de cambiar el esquema.
 */
enum class TransactionSource {
    ANKI_ANSWER,
    APP_UNLOCK,
    MANUAL_ADJUSTMENT
}
