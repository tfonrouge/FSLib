package com.fonrouge.fullStack.memoryDb

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable

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
 * Common container providing metadata for [TestItem].
 */
object CommonTestItem : ICommonContainer<TestItem, String, ApiFilter>(
    itemKClass = TestItem::class,
    filterKClass = ApiFilter::class,
    labelItem = "Item",
    labelList = "Items",
)
