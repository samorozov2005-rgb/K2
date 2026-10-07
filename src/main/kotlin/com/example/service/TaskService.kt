package com.example.service

import com.example.dto.CreateTaskRequest
import com.example.dto.UpdateTaskRequest
import com.example.model.Task
import com.example.repository.TaskRepository

class TaskService(private val repository: TaskRepository) {
    fun getAll(): List<Task> = repository.findAll()
    fun getById(id: Long): Task? = repository.findById(id)

    fun create(request: CreateTaskRequest): Task {
        validateTitle(request.title)
        return repository.create(request)
    }

    fun update(id: Long, request: UpdateTaskRequest): Task? {
        validateTitle(request.title)
        return repository.update(id, request)
    }

    fun delete(id: Long): Boolean = repository.delete(id)

    private fun validateTitle(title: String) {
        require(title.isNotBlank()) { "Title must not be blank" }
    }
}

