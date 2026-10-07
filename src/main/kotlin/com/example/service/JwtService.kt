package com.example.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.User
import io.ktor.server.config.ApplicationConfig
import java.time.Instant
import java.util.Date

class JwtService(config: ApplicationConfig) {
    private val secret = System.getenv("JWT_SECRET")
        ?: config.propertyOrNull("jwt.secret")?.getString()
        ?: "local-development-secret-change-in-production"
    val issuer: String = config.propertyOrNull("jwt.issuer")?.getString() ?: "ktor-task-app"
    val audience: String = config.propertyOrNull("jwt.audience")?.getString() ?: "ktor-task-users"
    val realm: String = config.propertyOrNull("jwt.realm")?.getString() ?: "Task API"
    val expiresInSeconds: Long = config.propertyOrNull("jwt.expiresInSeconds")?.getString()?.toLong() ?: 3600
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createToken(user: User): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", user.id)
        .withClaim("username", user.username)
        .withExpiresAt(Date.from(Instant.now().plusSeconds(expiresInSeconds)))
        .sign(algorithm)
}
