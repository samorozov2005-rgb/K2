package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class UserResponse(val id: Long, val username: String)

@Serializable
data class LoginResponse(val token: String)

