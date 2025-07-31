package com.fonrouge.fsLib.lib

/**
 * Executes the given block if the collection is not null and not empty.
 *
 * @param block The lambda to execute if the collection is not null and not empty. It receives the non-null and non-empty collection as a parameter.
 */
@Suppress("unused")
fun <T : Collection<E>, E : Any> (T?).ifNotIsNullOrEmpty(block: (T) -> Unit) {
    if (this != null && this.isNotEmpty()) block(this)
}
