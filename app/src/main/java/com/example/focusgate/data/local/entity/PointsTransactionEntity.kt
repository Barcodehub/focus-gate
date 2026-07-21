package com.example.focusgate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points_transactions")
data class PointsTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val delta: Int,
    val reason: String,
    val source: String
)
