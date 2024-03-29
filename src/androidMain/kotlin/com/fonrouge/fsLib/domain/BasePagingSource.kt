package com.fonrouge.fsLib.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.viewModel.ViewModelList
import java.io.IOException

class BasePagingSource<CC : ICommonContainer<T, ID, FILT, *>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    val viewModel: ViewModelList<CC, T, ID, FILT>,
) : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(anchorPosition = it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val nextPage = params.key ?: 1
            viewModel.refreshingList.value = true
            val list = viewModel.listStateGetter(nextPage)
            viewModel.refreshingList.value = false
            LoadResult.Page(
                data = list.data,
                prevKey = if (nextPage == 1) null else nextPage - 1,
                nextKey = list.last_page?.let { if (nextPage < it) nextPage + 1 else null }
            )
        } catch (e: IOException) {
            viewModel.pushSimpleState(SimpleState(isOk = false, msgError = e.localizedMessage))
//            return LoadResult.Error(e)
            return LoadResult.Page(
                data = listOf(),
                prevKey = null,
                nextKey = null,
            )
        } finally {
            viewModel.refreshingList.value = false
        }
    }
}
