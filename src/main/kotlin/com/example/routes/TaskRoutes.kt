package com.example.routes

import com.example.dto.CreateTaskRequest
import com.example.dto.ErrorResponse
import com.example.dto.UpdateTaskRequest
import com.example.service.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoutes(service: TaskService) {
    route("/api/v1/tasks") {
        get {
            call.respond(service.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"].toPositiveId()
            val task = service.getById(id)
            if (task == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
            } else {
                call.respond(task)
            }
        }
    }

    authenticate("auth-jwt") {
        route("/api/v1/tasks") {
            post {
                val request = call.receive<CreateTaskRequest>()
                call.respond(HttpStatusCode.Created, service.create(request))
            }

            put("/{id}") {
                val id = call.parameters["id"].toPositiveId()
                val request = call.receive<UpdateTaskRequest>()
                val updated = service.update(id, request)
                if (updated == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
                } else {
                    call.respond(updated)
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"].toPositiveId()
                if (!service.delete(id)) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
                } else {
                    call.respondText("", status = HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun String?.toPositiveId(): Long {
    val id = this?.toLongOrNull()
    require(id != null && id > 0) { "Invalid task id" }
    return id
}

