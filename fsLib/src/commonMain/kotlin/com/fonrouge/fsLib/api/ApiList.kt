package com.fonrouge.fsLib.api

import dev.kilua.rpc.RemoteFilter
import dev.kilua.rpc.RemoteSorter
import kotlinx.serialization.Serializable

/**
 * Represents a list of API items with various pagination, filtering, and sorting options.
 *
 * @param tabPage the current page number.
 * @param tabSize the number of items per page.
 * @param tabFilter a list of filters to apply to the API items.
 * @param tabSorter a list of sorters to apply to the API items.
 * @param sorter a string representation of the sorter to apply.
 * @param contentHashCode a hash code representing the content for cache purposes.
 * @param apiFilter the filter criteria used for the API.
 */
@Serializable
data class ApiList<FILT : IApiFilter<*>>(
    var tabPage: Int? = null,
    var tabSize: Int? = null,
    var tabFilter: List<RemoteFilter>? = null,
    var tabSorter: List<RemoteSorter>? = null,
    var sorter: String? = null,
    var contentHashCode: Int? = null,
    var apiFilter: FILT,
)
