package com.fonrouge.fullStack.memoryDb

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/**
 * Simple test model for InMemoryRepository tests.
 */
@Serializable
data class TestItem(
    override val _id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val active: Boolean = true,
) : BaseDoc<String>

/**
 * API filter for tests.
 */
@Serializable
class TestFilter : IApiFilter<Unit>()

/**
 * Common container providing metadata for [TestItem].
 */
object CommonTestItem : ICommonContainer<TestItem, String, TestFilter>(
    itemKClass = TestItem::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = TestFilter.serializer(),
    labelItem = "Item",
    labelList = "Items",
)
