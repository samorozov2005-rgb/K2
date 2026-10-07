package com.example.repository

import com.example.dto.CreateTaskRequest
import com.example.dto.UpdateTaskRequest
import com.example.model.Task
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class TaskRepository {
    private val tasks = ConcurrentHashMap<Long, Task>()
    private val nextId = AtomicLong(1)

    fun findAll(): List<Task> = tasks.values.sortedBy { it.id }

    fun findById(id: Long): Task? = tasks[id]

    fun create(request: CreateTaskRequest): Task {
        val task = Task(nextId.getAndIncrement(), request.title.trim(), request.description, request.completed)
        tasks[task.id] = task
        return task
    }

    fun update(id: Long, request: UpdateTaskRequest): Task? = tasks.computeIfPresent(id) { _, _ ->
        Task(id, request.title.trim(), request.description, request.completed)
    }

    fun delete(id: Long): Boolean = tasks.remove(id) != null
}

