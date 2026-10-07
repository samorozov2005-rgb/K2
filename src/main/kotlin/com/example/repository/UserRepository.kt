package com.example.repository

import com.example.model.User
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class UserRepository {
    private val usersByUsername = ConcurrentHashMap<String, User>()
    private val usersById = ConcurrentHashMap<Long, User>()
    private val nextId = AtomicLong(1)

    fun findByUsername(username: String): User? = usersByUsername[username]

    fun findById(id: Long): User? = usersById[id]

    fun create(username: String, passwordHash: String): User? {
        val user = User(nextId.getAndIncrement(), username, passwordHash)
        val existing = usersByUsername.putIfAbsent(username, user)
        if (existing != null) return null
        usersById[user.id] = user
        return user
    }
}

