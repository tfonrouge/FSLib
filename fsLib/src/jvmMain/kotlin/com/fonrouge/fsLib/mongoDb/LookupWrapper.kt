package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlin.reflect.KProperty1

/**
 * A generic wrapper class that holds a list of nested LookupWrapper instances.
 *
 * This class is designed for wrapping documents that extend from the BaseDoc interface,
 * aiding in frontend-backend data transactions.
 *
 * @param T The primary document type extending from BaseDoc.
 * @param U The type of documents held in the nested LookupWrapper instances, also extending from BaseDoc.
 * @property lookupWrappers A list of nested LookupWrapper instances.
 */
open class LookupWrapper<T : BaseDoc<*>, U : BaseDoc<*>>(
    open val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
)

/**
 * A specialized LookupWrapper that facilitates property-based lookup between documents.
 *
 * This class extends LookupWrapper to allow for lookups by a specified property.
 * It supports nested lookups by holding a list of LookupWrapper instances.
 *
 * @param T The type of the primary document, which must extend from BaseDoc.
 * @param U The type of the related document referred by the property, which must also extend from BaseDoc.
 * @property resultProperty A reference to the property of the primary document used for lookups.
 * @property lookupWrappers A list of nested LookupWrapper instances for further lookups.
 */
class LookupByProperty<T : BaseDoc<*>, U : BaseDoc<*>>(
    val resultProperty: KProperty1<T, U?>,
    override val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
) : LookupWrapper<T, U>()

/**
 * A class for building and executing lookup pipelines in a MongoDB aggregation framework.
 *
 * The class is a specialized form of [LookupWrapper] that constructs complex lookup pipelines
 * using a [LookupPipelineBuilder] instance. This allows for nested lookups and aggregations
 * within MongoDB collections.
 *
 * @param T The type of the local documents extending [BaseDoc].
 * @param U The type of the foreign documents to lookup, also extending [BaseDoc].
 * @param ID The type of the identifier used in the documents.
 * @param pipeline The pipeline builder responsible for creating the lookup stages.
 * @param lookupWrappers A list of nested [LookupWrapper] instances to facilitate multilevel lookups.
 */
class LookupByPipeline<T : BaseDoc<*>, U : BaseDoc<ID>, ID : Any>(
    val pipeline: LookupPipelineBuilder<T, U, ID>,
    override val lookupWrappers: List<LookupWrapper<U, *>> = emptyList()
) : LookupWrapper<T, U>()
