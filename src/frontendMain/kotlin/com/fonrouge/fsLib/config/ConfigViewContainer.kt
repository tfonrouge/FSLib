package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.StringId
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseDoc<U>, V : ViewDataContainer<*>, U : Any>(
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
    fun encodedId(_id: U?): String {
        return when {
            _id != null && idKClass != null -> Json.encodeToString(idKClass.serializer(), _id)
            _id != null && _id is OId<*> -> JSON.stringify(_id.id)
            _id != null && _id is StringId<*> -> JSON.stringify(_id.id)
            _id != null && _id is IntId<*> -> JSON.stringify(_id.id)
            else -> JSON.stringify(_id)
        }
    }
}
