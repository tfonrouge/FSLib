package com.fonrouge.ssr.bind

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.ssr.model.BindResult
import com.fonrouge.ssr.model.FieldDef
import com.fonrouge.ssr.model.FieldValidation
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * Binds HTTP form parameters to a [BaseDoc] instance using Kotlin reflection.
 *
 * v0.1 supports flat objects with the following property types:
 * [String], [Int], [Long], [Double], [Boolean], [LocalDate], [LocalDateTime],
 * [ObjectId], [Id] (KMongo), and enums.
 *
 * @param T the target model type
 * @param ID the model's ID type
 * @param kClass the KClass of the target model
 * @param fields the field definitions for validation
 */
class FormBinder<T : BaseDoc<ID>, ID : Any>(
    private val kClass: KClass<T>,
    private val fields: List<FieldDef<T>>,
) {
    /**
     * Binds form [parameters] to an instance of [T], validates against field definitions,
     * and returns a [BindResult].
     *
     * @param parameters the HTTP form parameters
     * @param existing an existing instance for update operations (provides default values for omitted fields)
     */
    fun bindAndValidate(parameters: Parameters, existing: T? = null): BindResult<T> {
        val errors = mutableMapOf<String, MutableList<String>>()
        val rawValues = mutableMapOf<String, String>()

        // Capture raw values for re-rendering
        fields.forEach { field ->
            val raw = parameters[field.name]
            if (raw != null) rawValues[field.name] = raw
        }

        // Validate fields
        fields.forEach { field ->
            val raw = parameters[field.name]
            val fieldErrors = validate(field, raw)
            if (fieldErrors.isNotEmpty()) {
                errors[field.name] = fieldErrors.toMutableList()
            }
        }

        if (errors.isNotEmpty()) {
            return BindResult(value = null, errors = errors, rawValues = rawValues)
        }

        // Bind to constructor
        val constructor = kClass.primaryConstructor
            ?: return BindResult(
                value = null,
                errors = mapOf("_global" to listOf("No primary constructor found for ${kClass.simpleName}")),
                rawValues = rawValues,
            )

        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val paramName = param.name ?: continue
            val raw = parameters[paramName]
            val fieldDef = fields.find { it.propertyName == paramName }

            if (raw == null || (raw.isBlank() && param.type.isMarkedNullable)) {
                // Use existing value for updates, or null/default
                if (existing != null) {
                    val existingValue = getPropertyValue(existing, paramName)
                    args[param] = existingValue
                } else if (param.isOptional) {
                    // Skip — let the default value apply
                    continue
                } else if (param.type.isMarkedNullable) {
                    args[param] = null
                } else if (raw != null && raw.isBlank() && isStringType(param)) {
                    args[param] = ""
                } else if (param.isOptional) {
                    continue
                } else {
                    // No value, not optional, not nullable — skip and let constructor handle it
                    continue
                }
                continue
            }

            val converted = convertValue(raw, param, fieldDef)
            if (converted.isFailure) {
                errors.getOrPut(paramName) { mutableListOf() }
                    .add(converted.exceptionOrNull()?.message ?: "Invalid value")
            } else {
                args[param] = converted.getOrNull()
            }
        }

        if (errors.isNotEmpty()) {
            return BindResult(value = null, errors = errors, rawValues = rawValues)
        }

        return try {
            BindResult(value = constructor.callBy(args), rawValues = rawValues)
        } catch (e: Exception) {
            BindResult(
                value = null,
                errors = mapOf("_global" to listOf("Failed to create instance: ${e.message}")),
                rawValues = rawValues,
            )
        }
    }

    /**
     * Validates a single field value against its [FieldDef.validators].
     */
    private fun validate(field: FieldDef<T>, raw: String?): List<String> {
        val errors = mutableListOf<String>()
        for (v in field.validators) {
            when (v) {
                is FieldValidation.Required -> {
                    if (raw.isNullOrBlank()) errors.add("${field.label} is required")
                }
                is FieldValidation.Email -> {
                    if (!raw.isNullOrBlank() && !raw.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
                        errors.add("${field.label} must be a valid email address")
                    }
                }
                is FieldValidation.MaxLength -> {
                    if (!raw.isNullOrBlank() && raw.length > v.max) {
                        errors.add("${field.label} must be at most ${v.max} characters")
                    }
                }
                is FieldValidation.MinLength -> {
                    if (!raw.isNullOrBlank() && raw.length < v.min) {
                        errors.add("${field.label} must be at least ${v.min} characters")
                    }
                }
                is FieldValidation.Pattern -> {
                    if (!raw.isNullOrBlank() && !raw.matches(Regex(v.regex))) {
                        errors.add(v.message)
                    }
                }
                is FieldValidation.Custom -> {
                    val result = v.check(raw)
                    if (result != null) errors.add(result)
                }
            }
        }
        return errors
    }

    /**
     * Converts a raw string form value to the type expected by the constructor parameter.
     */
    private fun convertValue(raw: String, param: KParameter, fieldDef: FieldDef<T>?): Result<Any?> {
        if (raw.isBlank()) {
            return if (param.type.isMarkedNullable) Result.success(null)
            else if (isStringType(param)) Result.success("")
            else if (param.isOptional) Result.success(null)
            else Result.failure(IllegalArgumentException("Value is required"))
        }

        val classifier = param.type.classifier as? KClass<*>
            ?: return Result.failure(IllegalArgumentException("Unknown type"))

        return when {
            classifier == String::class -> Result.success(raw)
            classifier == Int::class -> TypeConverters.toInt(raw)
            classifier == Long::class -> TypeConverters.toLong(raw)
            classifier == Double::class -> TypeConverters.toDouble(raw)
            classifier == Float::class -> TypeConverters.toDouble(raw).map { (it as Double).toFloat() }
            classifier == Boolean::class -> TypeConverters.toBoolean(raw)
            classifier == LocalDate::class -> TypeConverters.toLocalDate(raw)
            classifier == LocalDateTime::class -> TypeConverters.toLocalDateTime(raw)
            classifier == ObjectId::class -> TypeConverters.toObjectId(raw)
            classifier.java.isEnum -> TypeConverters.toEnum(raw, classifier.java)
            // KMongo Id types — try ObjectId-based construction
            Id::class.java.isAssignableFrom(classifier.java) -> TypeConverters.toObjectId(raw)
            else -> Result.failure(
                IllegalArgumentException("Unsupported type: ${classifier.simpleName}")
            )
        }
    }

    /**
     * Checks if the parameter type is String.
     */
    private fun isStringType(param: KParameter): Boolean {
        return (param.type.classifier as? KClass<*>) == String::class
    }

    /**
     * Gets a property value from an instance by property name using reflection.
     */
    private fun getPropertyValue(instance: T, propertyName: String): Any? {
        return try {
            val prop = kClass.members.find { it.name == propertyName }
            prop?.call(instance)
        } catch (_: Exception) {
            null
        }
    }
}
