package com.fonrouge.fsLib.viewModel

/*
@Suppress("unused")
class ViewModelListFactory<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter>(
    private val commonContainer: CC,
    private val listStateFun: KSuspendFunction1<ApiList<FILT>, ListState<T>>,
    private val itemStateFun: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>? = null
) : ViewModelProvider.NewInstanceFactory() {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return ViewModelList(
            commonContainer = commonContainer,
            listStateFun = listStateFun,
            itemStateFun = itemStateFun,
        ) as VM
    }
}
*/
