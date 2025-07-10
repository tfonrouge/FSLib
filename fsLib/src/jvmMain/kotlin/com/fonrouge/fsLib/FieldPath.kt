package com.fonrouge.fsLib

import com.fonrouge.fsLib.model.base.BaseDoc
import org.litote.kmongo.path
import kotlin.jvm.internal.PropertyReference1Impl
import kotlin.reflect.*

/**
 * Represents a path of fields in a document, supporting nested properties.
 *
 * @param T The type of the base document that extends [BaseDoc].
 * @param R The type of the property at the current level in the path.
 * @property previous The preceding path segment, or null if this is the root.
 * @property property The current property in the path.
 */
data class FieldPath<T : BaseDoc<*>, R>(
    val previous: FieldPath<T, *>?,
    val property: KProperty1<*, R?>,
) : KProperty1<T, R> {

    @Suppress("UNCHECKED_CAST")
    val kClass: KClass<out BaseDoc<*>> = (property as PropertyReference1Impl).owner as KClass<out BaseDoc<*>>
    val owner: KClass<out BaseDoc<*>> = previous?.kClass ?: kClass

//    val baseDocOwner get() = if (owner is BaseDoc<*>) owner as? BaseDoc<*> else null

    //?: (property as PropertyReference1Impl).owner as KClass<*>

    /*
        internal constructor(previous: KProperty1<*, Any?>, property: KProperty1<*, R?>) :
                this(
                    if (previous is FieldPath<*, *>) {
                        previous as FieldPath<T, *>?
                    } else {
                        FieldPath<T, Any?>(
                            null as (FieldPath<T, *>?),
                            previous
                        )
                    },
                    property
                )
    */

    internal val path: String
        get() = "${previous?.path?.let { "$it." } ?: ""}${property.path()}"

    override val getter: KProperty1.Getter<T, R>
        get() = TODO("Not yet implemented")

    override fun get(receiver: T): R {
        TODO("Not yet implemented")
    }

    override fun getDelegate(receiver: T): Any? {
        TODO("Not yet implemented")
    }

    override val isConst: Boolean
        get() = TODO("Not yet implemented")
    override val isLateinit: Boolean
        get() = TODO("Not yet implemented")
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")
    override val isFinal: Boolean
        get() = TODO("Not yet implemented")
    override val isOpen: Boolean
        get() = TODO("Not yet implemented")
    override val isSuspend: Boolean
        get() = TODO("Not yet implemented")
    override val name: String
        get() = path
    override val parameters: List<KParameter>
        get() = TODO("Not yet implemented")
    override val returnType: KType
        get() = TODO("Not yet implemented")
    override val typeParameters: List<KTypeParameter>
        get() = TODO("Not yet implemented")
    override val visibility: KVisibility
        get() = TODO("Not yet implemented")

    override fun call(vararg args: Any?): R {
        TODO("Not yet implemented")
    }

    override fun callBy(args: Map<KParameter, Any?>): R {
        TODO("Not yet implemented")
    }

    override val annotations: List<Annotation>
        get() = previous?.owner?.annotations ?: emptyList()

    override fun invoke(p1: T): R {
        TODO("Not yet implemented")
    }
}

/**
 * Combines two nullable properties of `KProperty1` type and returns a new [FieldPath] path.
 *
 * @param T0 The base document type that extends `BaseDoc<*>`.
 * @param T1 The type of the first property.
 * @param T2 The type of the second property.
 * @param next The next property in the path.
 * @return A new [FieldPath] instance representing the combined property path.
 */
operator fun <T0 : BaseDoc<*>, T1, T2> KProperty1<T0, T1?>.plus(next: KProperty1<T1, T2?>): KProperty1<T0, T2?> =
//    FieldPath(this, next)
    FieldPath(FieldPath(null, this), next)
