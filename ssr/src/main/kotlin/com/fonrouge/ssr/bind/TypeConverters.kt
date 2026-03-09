package com.fonrouge.ssr.bind

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.litote.kmongo.id.ObjectIdGenerator

/**
 * Type conversion functions for binding HTML form string values
 * to Kotlin property types. Each function returns a [Result] with
 * a descriptive error message on failure.
 */
object TypeConverters {

    /** Converts a string to [Int]. */
    fun toInt(value: String): Result<Any> = runCatching {
        value.toInt()
    }.recoverCatching {
        throw IllegalArgumentException("Must be a whole number")
    }

    /** Converts a string to [Long]. */
    fun toLong(value: String): Result<Any> = runCatching {
        value.toLong()
    }.recoverCatching {
        throw IllegalArgumentException("Must be a whole number")
    }

    /** Converts a string to [Double]. */
    fun toDouble(value: String): Result<Any> = runCatching {
        value.toDouble()
    }.recoverCatching {
        throw IllegalArgumentException("Must be a number")
    }

    /**
     * Converts a string to [Boolean].
     * HTML checkboxes submit "on" when checked and nothing when unchecked.
     */
    fun toBoolean(value: String): Result<Any> = Result.success(
        value.equals("true", ignoreCase = true) || value == "on" || value == "1"
    )

    /** Converts a string in ISO format (yyyy-MM-dd) to [LocalDate]. */
    fun toLocalDate(value: String): Result<Any> = runCatching {
        LocalDate.parse(value)
    }.recoverCatching {
        throw IllegalArgumentException("Must be a valid date (yyyy-MM-dd)")
    }

    /** Converts a string in ISO format to [LocalDateTime]. */
    fun toLocalDateTime(value: String): Result<Any> = runCatching {
        LocalDateTime.parse(value.replace(" ", "T"))
    }.recoverCatching {
        throw IllegalArgumentException("Must be a valid date and time")
    }

    /**
     * Converts a string to a KMongo ObjectId.
     * Accepts 24-character hex strings.
     */
    fun toObjectId(value: String): Result<Any> = runCatching {
        ObjectIdGenerator.create(value)
    }.recoverCatching {
        throw IllegalArgumentException("Must be a valid ObjectId (24 hex characters)")
    }

    /**
     * Converts a string to an enum value of the given class.
     * Tries matching by [Enum.name] first, then by ordinal.
     */
    fun toEnum(value: String, enumClass: Class<*>): Result<Any> = runCatching {
        val constants = enumClass.enumConstants
        constants?.firstOrNull { (it as Enum<*>).name == value }
            ?: constants?.firstOrNull { (it as Enum<*>).name.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Must be one of: ${constants?.joinToString { (it as Enum<*>).name }}")
    }
}
