package io.github.barcodehub.focusanki.domain.validation

/**
 * Límites de entrada compartidos por la UI y la capa de dominio. La UI los
 * usa para avisar al usuario; los casos de uso los aplican siempre, de modo
 * que ningún camino (UI, futuros importadores, etc.) pueda guardar valores
 * fuera de rango (costos negativos que regalan puntos, enteros que
 * desbordan el saldo, textos gigantes, etc.).
 */
object Limits {
    const val MAX_POINTS = 10_000
    const val MAX_ANKI_POINTS = 1_000
    const val MIN_COST = 1
    const val MIN_UNLOCK_MINUTES = 1
    const val MAX_UNLOCK_MINUTES = 240
    const val MAX_TITLE_LENGTH = 80
    const val MAX_DESCRIPTION_LENGTH = 300
}
