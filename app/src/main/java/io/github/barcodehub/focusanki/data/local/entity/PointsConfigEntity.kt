package io.github.barcodehub.focusanki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila única (id = 0) con la configuración de puntos actual. */
@Entity(tableName = "points_config")
data class PointsConfigEntity(
    @PrimaryKey val id: Int = 0,
    val again: Int,
    val hard: Int,
    val good: Int,
    val easy: Int,
    val unlockSessionMinutes: Int
)
