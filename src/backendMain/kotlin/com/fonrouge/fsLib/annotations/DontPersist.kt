package com.fonrouge.fsLib.annotations

/**
 * Simply set a null value on the item property before [CTableDb.updateOne] or [CTable.insertOne] funcs
 */
@Suppress("RedundantVisibilityModifier")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public actual annotation class DontPersist
