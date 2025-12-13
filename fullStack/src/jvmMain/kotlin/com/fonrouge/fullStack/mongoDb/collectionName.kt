package com.fonrouge.fullStack.mongoDb

import com.fonrouge.base.annotations.Collection
import com.fonrouge.base.model.BaseDoc
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

private val notThis = setOf(Any::class, BaseDoc::class)

/**
 * Retrieves the collection name specified by the `@Collection` annotation on the given class
 * or its superclasses. If the `@Collection` annotation is present, its `name` property is validated
 * to ensure it is not blank. The search continues through the inheritance hierarchy until a valid
 * collection name is found or no applicable annotation exists.
 *
 * @param self The class to inspect for the `@Collection` annotation or to search for the annotation
 *             in its superclasses.
 * @return The collection name specified by the `@Collection` annotation, or `null` if no valid
 *         annotation is found.
 */
private fun collectionNameFromAnnotationOrSupers(self: KClass<*>): String? {
    self.findAnnotation<Collection>()?.name
        ?.ifBlank { error("Collection name cannot be blank") }
        ?.let { return it }
    self.superclasses.forEach { klass ->
        if (klass !in notThis) {
            collectionNameFromAnnotationOrSupers(self = klass)?.let { return it }
        }
    }
    return null
}

/**
 * A thread-safe cache that stores the mapping of a Kotlin class to its corresponding collection name.
 * This map is used to improve performance by avoiding repetitive computation or retrieval
 * of collection names for the same class type.
 */
private val collectionNameCache: ConcurrentHashMap<KClass<*>, String> = ConcurrentHashMap()

/**
 * Clears the cache that stores collection names.
 *
 * This function removes all entries from the internal collection name cache, which may be used
 * for optimizing lookups or reusing previously retrieved collection names. Call this method
 * when the cached data is no longer valid or needs to be refreshed.
 */
@Suppress("unused")
fun clearCollectionNameCache() = collectionNameCache.clear()

/**
 * Retrieves the collection name associated with a given `KClass` that extends `BaseDoc`.
 *
 * This property provides a string representation of the collection name, which is determined as follows:
 * - If the class or its superclasses are annotated with `@Collection`, the `name` value from the annotation is used.
 * - If no explicit annotation is found, the `simpleName` of the class is used, transformed to start with a lowercase letter.
 * - Throws an error if the class does not have a `simpleName`.
 *
 * The result is cached for performance optimization using a class-level cache.
 */
val KClass<out BaseDoc<*>>.collectionName: String
    get() = collectionNameCache.computeIfAbsent(this) { kClass ->
        val simpleName = kClass.simpleName
            ?: error("Cannot determine collection name: $kClass has no simpleName; add @Collection(name=...)")

        collectionNameFromAnnotationOrSupers(kClass) ?: simpleName.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch else ch.lowercaseChar()
        }
    }

