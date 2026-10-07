package com.example

import com.example.config.configureErrorHandling
import com.example.config.configureSecurity
import com.example.config.configureSerialization
import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import com.example.routes.authRoutes
import com.example.routes.taskRoutes
import com.example.service.JwtService
import com.example.service.TaskService
import com.example.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val taskService = TaskService(TaskRepository())
    val userRepository = UserRepository()
    val userService = UserService(userRepository)
    val jwtService = JwtService(environment.config)

    configureSerialization()
    configureErrorHandling()
    configureSecurity(jwtService, userRepository)

    routing {
        authRoutes(userService, jwtService)
        taskRoutes(taskService)
    }
}

