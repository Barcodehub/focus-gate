package com.example.focusgate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val points: Int,
    val completed: Boolean,
    val createdAt: Long,
    val lastCompletedAt: Long?,
    val lastResetAt: Long?
)