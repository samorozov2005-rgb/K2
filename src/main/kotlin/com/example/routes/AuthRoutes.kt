package com.example.routes

import com.example.dto.ErrorResponse
import com.example.dto.LoginRequest
import com.example.dto.LoginResponse
import com.example.dto.RegisterRequest
import com.example.service.JwtService
import com.example.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(userService: UserService, jwtService: JwtService) {
    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = userService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userService.authenticate(request.username, request.password)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                return@post
            }
            call.respond(LoginResponse(jwtService.createToken(user)))
        }
    }
}

