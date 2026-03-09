package com.example.ssrsample.basic

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/**
 * A simple to-do item.
 */
@Serializable
data class Todo(
    override val _id: String = "",
    val title: String = "",
    val done: Boolean = false,
) : BaseDoc<String>

/**
 * API filter for Todo queries.
 */
@Serializable
class TodoFilter : IApiFilter<String>()

/**
 * Metadata container for [Todo].
 */
object CommonTodo : ICommonContainer<Todo, String, TodoFilter>(
    itemKClass = Todo::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = TodoFilter.serializer(),
    labelItem = "Todo",
    labelList = "Todos",
)
