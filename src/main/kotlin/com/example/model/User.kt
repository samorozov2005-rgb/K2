package com.example.model

data class User(
    val id: Long,
    val username: String,
    val passwordHash: String,
)

