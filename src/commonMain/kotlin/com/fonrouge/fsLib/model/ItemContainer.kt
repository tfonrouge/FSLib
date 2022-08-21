package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.KPair
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray

@Serializable
data class ItemContainer<T>(
    var item: T? = null,
    var result: Boolean = item != null,
    var description: String? = null,
    @Serializable(with = FSListKPairSerializer::class)
    var onCreateDefaultValue: List<KPair<T>>? = null,
)

class FSListKPairSerializer<E : KPair<*>>(private val elementSerializer: KSerializer<E>) : KSerializer<List<E>> {
    private val listSerializer = ListSerializer(elementSerializer = elementSerializer)
    override fun deserialize(decoder: Decoder): List<E> {
        return with(decoder as JsonDecoder) {
            print("DECODER")
            println(decoder)
            decodeJsonElement().jsonArray.mapNotNull {
                try {
                    json.decodeFromJsonElement(deserializer = elementSerializer, it)
                } catch (e: SerializationException) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    override val descriptor: SerialDescriptor
        get() = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<E>) {
        listSerializer.serialize(encoder, value)
    }
}