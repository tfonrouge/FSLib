package com.fonrouge.fsUtils.collections

import com.fonrouge.backendLib.mongoDb.Coll
import com.fonrouge.backendLib.mongoDb.IChangeLogColl
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.model.IUser
import com.fonrouge.base.offsetDateTimeNow
import com.fonrouge.base.serializers.FSOffsetDateTimeSerializer
import com.fonrouge.backendLib.common.ICommonChangeLog
import com.fonrouge.backendLib.model.IChangeLog
import com.fonrouge.fsUtils.model.IWorkLogBase
import io.ktor.server.sessions.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

abstract class IWorkLogBaseColl<ChgLogColl : IChangeLogColl<CmnChgLog, ChgLog, U, UID>, CmnChgLog : ICommonChangeLog<ChgLog, U, UID>, ChgLog : IChangeLog<U, UID>, U : IUser<UID>, UID : Any, CC : ICommonContainer<T, ID, FILT>, T : IWorkLogBase<ID>, ID : Any, FILT : IApiFilter<*>>(
    commonContainer: CC,
    val changeLogColl: () -> ChgLogColl,
) : Coll<CC, T, ID, FILT>(
    commonContainer = commonContainer
) {
    @OptIn(InternalSerializationApi::class)
    private fun buildChangeLog(apiItem: ApiItem<T, ID, FILT>, orig: T?): ChgLog? {
        val (items, action, user: U?) = when (apiItem) {
            is ApiItem.Action.Create<T, ID, FILT> -> Triple(
                apiItem.item to null,
                IChangeLog.Action.Create,
                apiItem.call?.sessions?.get(changeLogColl().commonContainerUser.itemKClass)
            )

            is ApiItem.Action.Update<T, ID, FILT> -> Triple(
                apiItem.item to orig,
                IChangeLog.Action.Update,
                apiItem.call?.sessions?.get(changeLogColl().commonContainerUser.itemKClass)
            )

            is ApiItem.Action.Delete<T, ID, FILT> -> Triple(
                apiItem.item to null,
                IChangeLog.Action.Delete,
                apiItem.call?.sessions?.get(changeLogColl().commonContainerUser.itemKClass)
            )

            else -> return null
        }
        val json1 = Json.encodeToJsonElement(commonContainer.itemSerializer, items.first) as JsonObject
        val orig = items.second?.let { Json.encodeToJsonElement(commonContainer.itemSerializer, it) as JsonObject }
        fun getValue(jsonElement: JsonElement?): String? {
            return when (jsonElement) {
                is JsonPrimitive -> if (jsonElement == JsonNull) null else jsonElement.content
                is JsonArray -> jsonElement.toString()
                is JsonObject -> jsonElement.toString()
                else -> null
            }
        }

        val data: Map<String, Pair<String?, String?>> = orig?.let {
            val map = mutableMapOf<String, Pair<String?, String?>>()
            json1.map { entry ->
                val v1 = getValue(entry.value)
                val v2 = getValue(orig[entry.key])
                if (v1 != v2) {
                    map[entry.key] = Pair(v1, v2)
                }
            }
            orig.map { entry ->
                val v1 = getValue(json1[entry.key])
                val v2 = getValue(entry.value)
                if (v1 != v2) {
                    map[entry.key] = Pair(v1, v2)
                }
            }
            map
        } ?: json1.mapValues {
            Pair(
                first = when (it.value) {
                    is JsonArray -> it.value.jsonArray.toString()
                    is JsonObject -> it.value.toString()
                    else -> it.value.jsonPrimitive.content
                },
                second = null
            )
        }
        return if (data.isNotEmpty()) {
            Json.decodeFromJsonElement(
                deserializer = changeLogColl().commonContainer.itemKClass.serializer(),
                element = buildJsonObject {
                    put(
                        "serializedId",
                        Json.encodeToString(serializer = commonContainer.idSerializer, value = items.first._id)
                    )
                    put("className", commonContainer.itemKClass.java.simpleName)
                    put("dateTime", Json.encodeToJsonElement(FSOffsetDateTimeSerializer, offsetDateTimeNow()))
                    put("action", Json.encodeToJsonElement(action))
                    put(
                        "clientInfo",
                        apiItem.call?.request?.let { "${it.headers["Origin"]}; ${it.headers["User-Agent"]}" })
                    put(
                        "userId",
                        user?._id?.let {
                            Json.encodeToJsonElement(
                                serializer = changeLogColl().commonContainerUser.idSerializer,
                                value = it
                            )
                        } ?: JsonNull)
                    put("userInfo", changeLogColl().userInfo(user))
                    put("data", Json.encodeToJsonElement(data))
                }
            )
        } else null
    }

    override suspend fun onAfterCreateAction(
        apiItem: ApiItem.Action.Create<T, ID, FILT>,
        result: Boolean,
    ) {
        changeLogColl().coroutine.insertOne(
            buildChangeLog(apiItem = apiItem, orig = null) ?: return
        )
    }

    override suspend fun onAfterUpdateAction(
        apiItem: ApiItem.Action.Update<T, ID, FILT>,
        orig: T,
        result: Boolean,
    ) {
        changeLogColl().coroutine.insertOne(
            buildChangeLog(apiItem = apiItem, orig = orig) ?: return
        )
    }

    override suspend fun onAfterDeleteAction(
        apiItem: ApiItem.Action.Delete<T, ID, FILT>,
        result: Boolean,
    ) {
        changeLogColl().coroutine.insertOne(
            buildChangeLog(apiItem = apiItem, orig = null) ?: return
        )
    }
}
