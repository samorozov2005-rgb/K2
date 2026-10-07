package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val completed: Boolean,
)

