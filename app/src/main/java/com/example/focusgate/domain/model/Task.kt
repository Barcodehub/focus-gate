package com.example.focusgate.domain.model

data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val points: Int,
    val completed: Boolean,
    val createdAt: Long,
    val lastCompletedAt: Long?,
    val lastResetAt: Long?
)