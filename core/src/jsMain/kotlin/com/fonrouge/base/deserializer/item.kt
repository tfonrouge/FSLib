package com.fonrouge.base.deserializer

import js.objects.Object
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

/**
 * Decodes the dynamic object into an instance of the specified type [T].
 *
 * This function uses Kotlinx Serialization to deserialize the object dynamically at runtime.
 * It requires the type [T] to have a corresponding serializer.
 *
 * The function is marked with `@OptIn(ExperimentalSerializationApi::class)` to utilize experimental serialization functionality.
 *
 * @param T The type to which the dynamic object will be deserialized. Must be a reified type.
 * @receiver The dynamic object to be deserialized.
 * @return An instance of type [T] obtained by decoding the dynamic object.
 * @throws SerializationException If deserialization fails or the dynamic object does not match the structure of type [T].
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Object.item() = Json.decodeFromDynamic<T>(this)

/**
 * Decodes a dynamic JSON object into an object of type T using the provided serializer.
 *
 * @param T The type of the object to decode.
 * @param serializer The serializer for the type T.
 * @receiver A dynamic JSON object to decode.
 * @return The decoded object of type T.
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> Object.item(serializer: KSerializer<T>) = Json.decodeFromDynamic(serializer, this)
