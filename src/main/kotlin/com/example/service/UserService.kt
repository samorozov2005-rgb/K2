package com.example.service

import com.example.dto.RegisterRequest
import com.example.dto.UserResponse
import com.example.model.User
import com.example.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

class DuplicateUsernameException : RuntimeException("Username is already registered")

class UserService(private val repository: UserRepository) {
    fun register(request: RegisterRequest): UserResponse {
        val username = request.username.trim()
        require(username.isNotEmpty()) { "Username must not be blank" }
        require(request.password.length >= 8) { "Password must contain at least 8 characters" }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(12))
        val user = repository.create(username, passwordHash) ?: throw DuplicateUsernameException()
        return UserResponse(user.id, user.username)
    }

    fun authenticate(username: String, password: String): User? {
        val user = repository.findByUsername(username.trim()) ?: return null
        return user.takeIf { BCrypt.checkpw(password, it.passwordHash) }
    }
}

