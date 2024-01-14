package com.fonrouge.fsLib.annotations

/**
 * Simply set a null value on the item property before Coll.updateOne or Coll.insertOne funcs
 */
@Suppress("RedundantVisibilityModifier", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public actual annotation class DontPersist
