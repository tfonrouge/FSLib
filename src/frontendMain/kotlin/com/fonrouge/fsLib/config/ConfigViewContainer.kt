package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseModel<U>, V : ViewDataContainer<*>, U : Any>(
    val idKClass: KClass<U>? = null,
    name: String,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String,
) : ConfigView<V>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    baseUrl = baseUrl,
) {
    @OptIn(InternalSerializationApi::class)
    fun encodedId(_id: U): String {
        return idKClass?.let { Json.encodeToString(it.serializer(), _id) } ?: JSON.stringify(_id)
    }
}
