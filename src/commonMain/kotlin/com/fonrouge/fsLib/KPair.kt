package com.fonrouge.fsLib

import io.kvision.types.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.reflect.KProperty1

@Serializable(with = FSKPairSerializer::class)
class KPair<T>(
    val kProp: KProperty1<T, @Contextual Any?>,
    @Contextual
    val value: Any?,
)

infix fun <T> KProperty1<T, Int?>.with(that: Int?) = KPair(this, that)
infix fun <T> KProperty1<T, Boolean?>.with(that: Boolean?) = KPair(this, that)
infix fun <T> KProperty1<T, String?>.with(that: String?) = KPair(this, that)
infix fun <T> KProperty1<T, LocalDateTime?>.with(that: LocalDateTime?) = KPair(this, that)
infix fun <T> KProperty1<T, Double?>.with(that: Double?) = KPair(this, that)
infix fun <T> KProperty1<T, Float?>.with(that: Float?) = KPair(this, that)
infix fun <T> KProperty1<T, Any>.withAny(that: Any) = KPair(this, that)

object FSKPairSerializer : KSerializer<KPair<*>> {
    override fun deserialize(decoder: Decoder): KPair<*> {
        decoder.decodeStructure(descriptor) {

        }
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("KPair") {
            element<String>("propertyName")
            element<String>("value")
        }

    override fun serialize(encoder: Encoder, value: KPair<*>) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value = value.kProp.name)
            encodeStringElement(descriptor, 1, value = value.value.toString())
        }
    }
}