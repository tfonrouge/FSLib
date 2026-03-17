package com.fonrouge.fullStack.repository

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Shared utility for copying instances using only their primary constructor parameters.
 *
 * fsLib enforces a convention: only primary constructor parameters of `BaseDoc` subclasses
 * are persisted. Body properties are intentionally stripped before database writes.
 * This object centralises that logic with cached reflection metadata so that every
 * repository engine (MongoDB, SQL, InMemory) shares the same implementation.
 *
 * Reflection metadata is computed once per [KClass] and cached in a [ConcurrentHashMap]
 * for efficient repeated use.
 */
object ConstructorCopier {

    /**
     * Cached reflection metadata for a given class.
     *
     * @param T The entity type.
     * @param constructor The primary constructor reference.
     * @param parameterNames Ordered list of primary constructor parameter names.
     * @param memberProperties Map of all member properties keyed by name, for value extraction.
     */
    private data class ConstructorMeta<T : Any>(
        val constructor: KFunction<T>,
        val parameterNames: List<String>,
        val memberProperties: Map<String, KProperty1<T, *>>,
    )

    /** Per-class cache of constructor metadata. */
    private val cache = ConcurrentHashMap<KClass<*>, ConstructorMeta<*>>()

    /**
     * Returns (or computes and caches) the [ConstructorMeta] for the given [kClass].
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> metaFor(kClass: KClass<T>): ConstructorMeta<T> =
        cache.getOrPut(kClass) {
            val ctor = kClass.primaryConstructor
                ?: error("${kClass.simpleName} has no primary constructor")
            ConstructorMeta(
                constructor = ctor,
                parameterNames = ctor.parameters.mapNotNull { it.name },
                memberProperties = kClass.memberProperties.associateBy { it.name },
            )
        } as ConstructorMeta<T>

    /**
     * Creates a copy of [instance] using only its primary constructor parameters,
     * effectively stripping any body properties.
     *
     * Optionally applies [fieldOverrides] to replace specific constructor parameter values.
     * Each key in [fieldOverrides] **must** be a primary constructor parameter name; passing
     * a body-property name will throw [IllegalArgumentException] (fail-fast guard for
     * `AssignTo` misuse).
     *
     * @param T The entity type.
     * @param kClass The [KClass] of [T].
     * @param instance The source instance to copy.
     * @param fieldOverrides Optional map of parameter name to override value.
     * @return A new instance of [T] constructed from primary constructor parameters only.
     * @throws IllegalArgumentException If any override key is not a constructor parameter.
     * @throws IllegalStateException If the class has no primary constructor.
     */
    fun <T : Any> copyWithConstructorParams(
        kClass: KClass<T>,
        instance: T,
        fieldOverrides: Map<String, Any?> = emptyMap(),
    ): T {
        val meta = metaFor(kClass)

        // Guard: all override keys must be constructor parameters
        fieldOverrides.keys.forEach { key ->
            require(key in meta.parameterNames) {
                "Field override '$key' is not a primary constructor parameter of ${kClass.simpleName}. " +
                        "Only constructor parameters can be overridden; body properties are not persisted."
            }
        }

        val values = meta.parameterNames.map { name ->
            if (fieldOverrides.containsKey(name)) {
                fieldOverrides[name]
            } else {
                meta.memberProperties[name]?.get(instance)
            }
        }

        return meta.constructor.call(*values.toTypedArray())
    }
}
