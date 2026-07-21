package com.example.focusgate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protected_apps")
data class ProtectedAppEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val costPoints: Int,
    val enabled: Boolean
)
