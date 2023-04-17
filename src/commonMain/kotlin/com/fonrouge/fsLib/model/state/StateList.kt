package com.fonrouge.fsLib.model.state

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
 * @param state can contain an arbitrary data which can be instantiated with [stateValue]
 */
@Serializable
data class StateList(
    var tabPage: Int? = null,
    var tabSize: Int? = null,
    var tabFilter: List<RemoteFilter>? = null,
    var tabSorter: List<RemoteSorter>? = null,
    var filter: String? = null,
    var sorter: String? = null,
    var params: String? = null,
    var checksum: String? = null,
    override var contextId: String? = null,
    override var contextClass: String? = null,
    override var state: String? = null,
) : ContexState()

@Suppress("unused")
val String?.stateList: StateList?
    get() {
        return this?.let { Json.decodeFromString<StateList>(it) }
    }
