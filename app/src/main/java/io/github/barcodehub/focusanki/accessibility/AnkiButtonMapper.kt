package io.github.barcodehub.focusanki.accessibility

import io.github.barcodehub.focusanki.domain.model.AnkiButton

/**
 * Convierte la información de un nodo clicado en AnkiDroid en un botón de
 * respuesta. Es deliberadamente estricto: antes se aceptaba cualquier texto
 * que *empezara* con "good"/"bien"/"hard"... (p. ej. un mazo llamado
 * "Easy English" o contenido de una tarjeta), lo que permitía sumar puntos
 * sin responder nada.
 *
 * Reglas:
 *  1. Si el id de vista termina en ease1..ease4 (ids reales del Reviewer),
 *     manda el id.
 *  2. Si no hay id, se acepta el texto solo si la PRIMERA línea es
 *     exactamente una de las etiquetas conocidas (los botones muestran
 *     "Good" y, debajo, el intervalo).
 */
object AnkiButtonMapper {

    private val AGAIN = setOf("again", "de nuevo", "otra vez")
    private val HARD = setOf("hard", "difícil", "dificil")
    private val GOOD = setOf("good", "bien")
    private val EASY = setOf("easy", "fácil", "facil")

    private val ID_REGEX = Regex("""ease([1-4])$""", RegexOption.IGNORE_CASE)

    fun map(viewId: String?, text: CharSequence?, contentDescription: CharSequence?): AnkiButton? {
        ID_REGEX.find(viewId.orEmpty())?.let {
            return when (it.groupValues[1]) {
                "1" -> AnkiButton.AGAIN
                "2" -> AnkiButton.HARD
                "3" -> AnkiButton.GOOD
                else -> AnkiButton.EASY
            }
        }
        // Con un id presente que no es de botón de respuesta, no hay nada que mapear.
        if (!viewId.isNullOrEmpty() && !viewId.contains("ease", ignoreCase = true)) return null
        return mapLabel(text) ?: mapLabel(contentDescription)
    }

    private fun mapLabel(raw: CharSequence?): AnkiButton? {
        val label = raw?.toString()?.lineSequence()?.firstOrNull()?.trim()?.lowercase() ?: return null
        return when (label) {
            in AGAIN -> AnkiButton.AGAIN
            in HARD -> AnkiButton.HARD
            in GOOD -> AnkiButton.GOOD
            in EASY -> AnkiButton.EASY
            else -> null
        }
    }
}
