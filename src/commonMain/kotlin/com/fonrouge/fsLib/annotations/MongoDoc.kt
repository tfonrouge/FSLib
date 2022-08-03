@file:Suppress("unused", "RedundantVisibilityModifier")

package com.fonrouge.fsLib.annotations

@Target(AnnotationTarget.CLASS)
public annotation class MongoDoc(val collection: String)
