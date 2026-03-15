package com.example.ssrsample.basic

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable

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
 * Metadata container for [Todo].
 */
object CommonTodo : ICommonContainer<Todo, String, ApiFilter>(
    itemKClass = Todo::class,
    filterKClass = ApiFilter::class,
    labelItem = "Todo",
    labelList = "Todos",
)
