package com.example.config

import com.example.dto.ErrorResponse
import com.example.repository.UserRepository
import com.example.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity(jwtService: JwtService, users: UserRepository) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtService.realm
            verifier(jwtService.verifier)
            validate { credential ->
                val userId = runCatching { credential.payload.getClaim("userId").asLong() }.getOrNull()
                if (userId != null && userId > 0 && users.findById(userId) != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
            }
        }
    }
}
