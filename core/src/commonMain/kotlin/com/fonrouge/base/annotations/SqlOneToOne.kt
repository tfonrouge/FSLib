package com.fonrouge.base.annotations

/**
 * Annotation to denote a one-to-one relationship in SQL operations.
 *
 * This annotation can be applied to properties within a class to specify that these
 * properties represent a one-to-one relationship with another entity in the database.
 * It is used by ORM frameworks and SQL mappers to handle such relationships appropriately
 * during serialization and deserialization tasks.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlOneToOne
