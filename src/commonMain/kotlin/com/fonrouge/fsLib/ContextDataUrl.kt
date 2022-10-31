package com.fonrouge.fsLib

import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Data structure passed to backend which contains parameters from tabulator (frontEnd)
 *
 * @param contextClass the class name for the master item (if any) of the data list
 * @param contextId the id for the item in the master item (if any)
 * @param state can contain a arbitrary data which can be instantiated with [stateValue]
 */
@Serializable
data class ContextDataUrl(
    var tabPage: Int? = null,
    var tabSize: Int? = null,
    var tabFilter: List<RemoteFilter>? = null,
    var tabSorter: List<RemoteSorter>? = null,
    var filter: String? = null,
    var sorter: String? = null,
    var contextClass: String? = null,
    var contextId: String? = null,
    var params: String? = null,
    var checksum: String? = null,
    var state: String? = null,
) {
    @Suppress("unused")
    inline fun <reified T> contextIdValue(): T? {
        return contextId?.let { Json.decodeFromString(it) }
    }

    @Suppress("unused")
    inline fun <reified T> stateValue(): T? {
        return state?.let { Json.decodeFromString(it) }
    }
}

@Suppress("unused")
val String?.contextDataUrl: ContextDataUrl?
    get() {
        return this?.let { Json.decodeFromString<ContextDataUrl>(it) }
    }
