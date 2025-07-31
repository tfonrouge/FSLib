package com.fonrouge.fsLib.lib

/**
 * Executes the given block if the collection is not empty.
 *
 * This extension function checks whether the collection contains any elements. If the collection
 * is not empty, the specified block is executed with the collection passed as its argument.
 *
 * @param block A lambda function to execute if the collection is not empty.
 * It receives the calling collection as its parameter.
 */
@Suppress("unused")
fun <T : Collection<E>, E : Any, R> (T).ifIsNotEmpty(block: (T) -> R): R? {
    return if (isNotEmpty()) block(this) else null
}
