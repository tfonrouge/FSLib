package com.fonrouge.fsLib.viewModel

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fonrouge.fsLib.apiServices.AppApi
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.domain.BasePagingSource
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.ListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KSuspendFunction1

abstract class ViewModelList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> :
    ViewModelContainer<CC, T, ID, FILT>() {
    companion object {
        var lastRequest: Long = 0L
    }

    private var filterSerialized: FILT? = null
    open val pageSize: MutableIntState = mutableIntStateOf(20)
    val refreshingList: MutableState<Boolean> = mutableStateOf(false)
    var requestRefresh by mutableStateOf(false)
    val refreshByFilter = mutableStateOf(false)
    abstract var apiFilter: FILT
    abstract val listStateFun: KSuspendFunction1<ApiList<FILT>, ListState<T>>
    override val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>? = null
    open val onBeforeListStateGet: (() -> Unit)? = null

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    suspend fun listStateGetter(pageNum: Int): ListState<T> {
        if (AppApi.delayBeforeRequest > 0) delay(AppApi.delayBeforeRequest.toLong())
        onBeforeListStateGet?.invoke()
        lastRequest = System.currentTimeMillis()
        return listStateFun(
            ApiList(
                tabPage = pageNum,
                tabSize = pageSize.intValue,
                apiFilter = apiFilter
            )
        )
    }

    val flowPagingData: Flow<PagingData<T>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = pageSize.intValue,
            ),
            pagingSourceFactory = {
                BasePagingSource(
                    viewModel = this,
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    open fun onEvent(uiBaseEvent: UIBaseEvent) {
        when (uiBaseEvent) {
            UIBaseEvent.EditingFilter -> {
                if (!refreshByFilter.value) {
                    filterSerialized = apiFilter
                    refreshByFilter.value = true
                }
            }

            UIBaseEvent.RefreshByFilter -> {
                refreshByFilter.value = false
                if (filterSerialized?.equals(apiFilter) != true) {
                    filterSerialized = apiFilter
                    requestRefresh = true
                }
            }
        }
    }

    sealed class UIBaseEvent {
        data object EditingFilter : UIBaseEvent()
        data object RefreshByFilter : UIBaseEvent()
    }
}
