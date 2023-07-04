package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.StringId
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class ConfigViewContainer<T : BaseDoc<ID>, V : ViewDataContainer<FILT>, ID : Any, FILT : ApiFilter>(
    val itemKClass: KClass<T>,
    val idKClass: KClass<ID>? = null,
    apiFilterKClass: KClass<FILT>,
    name: String,
    label: String,
    viewFunc: KClass<out V>,
    baseUrl: String,
) : ConfigView<V, FILT>(
    name = name,
    label = label,
    viewFunc = viewFunc,
    apiFilterKClass = apiFilterKClass,
    baseUrl = baseUrl,
) {
    @OptIn(InternalSerializationApi::class)
    fun encodedId(id: ID?): String {
        return when {
            id != null && idKClass != null -> Json.encodeToString(idKClass.serializer(), id)
            id != null && id is OId<*> -> JSON.stringify(id.id)
            id != null && id is StringId<*> -> JSON.stringify(id.id)
            id != null && id is IntId<*> -> JSON.stringify(id.id)
            else -> JSON.stringify(id)
        }
    }
}
