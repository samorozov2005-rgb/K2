package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val completed: Boolean,
)

@Serializable
data class UpdateTaskRequest(
    val title: String,
    val description: String,
    val completed: Boolean,
)

