package com.fonrouge.fsLib.model.apiData

import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter
import kotlinx.serialization.Serializable

/**
 * Data structure passed to backend which contains parameters from tabulator (frontEnd)
 *
 * @param FILT [IApiFilter] type param
 */
@Serializable
data class ApiList<FILT : IApiFilter>(
    var tabPage: Int = 1,
    var tabSize: Int = 10,
    var tabFilter: List<RemoteFilter>? = null,
    var tabSorter: List<RemoteSorter>? = null,
    var sorter: String? = null,
    var params: String? = null,
    var contentHashCode: Int? = null,
    var apiFilter: FILT? = null,
)
