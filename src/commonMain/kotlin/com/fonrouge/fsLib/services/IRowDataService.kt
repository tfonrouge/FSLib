package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.remote.RemoteData
import io.kvision.remote.RemoteFilter
import io.kvision.remote.RemoteSorter

@Suppress("unused")
interface IRowDataService {
    suspend fun rowData(
        page: Int?,
        size: Int?,
        filter: List<RemoteFilter>?,
        sorter: List<RemoteSorter>?,
        state: String?
    ): RemoteData<out BaseModel<*>>
}
