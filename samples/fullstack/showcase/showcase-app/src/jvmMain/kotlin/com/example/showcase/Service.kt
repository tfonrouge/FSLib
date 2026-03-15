package com.example.showcase

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.fullStack.memoryDb.InMemoryRepository
import com.fonrouge.fullStack.services.StandardCrudService

/**
 * Server-side implementation of [ITaskService].
 * Delegates all operations to the [InMemoryRepository] via [StandardCrudService].
 */
class TaskService(
    repo: InMemoryRepository<Task, String, ApiFilter, String>,
) : StandardCrudService<Task, String, ApiFilter>(repo), ITaskService
