package io.github.barcodehub.focusanki

import io.github.barcodehub.focusanki.accessibility.AnkiButtonMapper
import io.github.barcodehub.focusanki.domain.model.AnkiButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnkiButtonMapperTest {

    @Test fun `ids de los botones reales se mapean`() {
        assertEquals(AnkiButton.AGAIN, AnkiButtonMapper.map("com.ichi2.anki:id/flashcard_layout_ease1", null, null))
        assertEquals(AnkiButton.HARD, AnkiButtonMapper.map("com.ichi2.anki:id/flashcard_layout_ease2", null, null))
        assertEquals(AnkiButton.GOOD, AnkiButtonMapper.map("com.ichi2.anki:id/ease3", "x", null))
        assertEquals(AnkiButton.EASY, AnkiButtonMapper.map("com.ichi2.anki:id/ease4", null, null))
    }

    @Test fun `texto exacto sin id funciona, incluida la primera linea`() {
        assertEquals(AnkiButton.GOOD, AnkiButtonMapper.map(null, "Good", null))
        assertEquals(AnkiButton.EASY, AnkiButtonMapper.map(null, "  Fácil\n4d", null))
        assertEquals(AnkiButton.AGAIN, AnkiButtonMapper.map(null, "De nuevo", null))
    }

    @Test fun `textos que solo empiezan con la palabra NO puntuan`() {
        assertNull(AnkiButtonMapper.map(null, "Easy English", null))
        assertNull(AnkiButtonMapper.map(null, "Bienvenido", null))
        assertNull(AnkiButtonMapper.map(null, "hardware", null))
        assertNull(AnkiButtonMapper.map(null, "goodbye", null))
        assertNull(AnkiButtonMapper.map(null, "Mostrar respuesta", null))
    }

    @Test fun `un id que no es de respuesta no se mapea aunque el texto coincida`() {
        assertNull(AnkiButtonMapper.map("com.ichi2.anki:id/deck_name", "Easy", null))
    }

    @Test fun `entradas nulas o vacias no puntuan`() {
        assertNull(AnkiButtonMapper.map(null, null, null))
        assertNull(AnkiButtonMapper.map("", "", ""))
    }
}
