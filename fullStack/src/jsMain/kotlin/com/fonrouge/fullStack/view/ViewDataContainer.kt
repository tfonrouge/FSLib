package com.fonrouge.fullStack.view

import com.fonrouge.fullStack.callItemService
import com.fonrouge.fullStack.config.ConfigViewContainer
import com.fonrouge.base.api.CallType
import com.fonrouge.base.api.CrudTask
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.lib.toast
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.state.ItemState
import io.kvision.i18n.gettext
import io.kvision.modal.Confirm
import io.kvision.modal.ModalSize
import io.kvision.toast.Toast
import kotlinx.browser.window
import kotlin.js.Date

/**
 * An abstract class `ViewDataContainer` which extends from the `View`. This class is designed
 * to manage the configuration and periodic update of a view container.
 *
 * @param CC The type of the common container must extend from `ICommonContainer`.
 * @param T The type of the data item must extend from `BaseDoc`.
 * @param ID The type out of the ID of a data item, which must be a non-nullable type.
 * @param FILT The type of the API filter used for querying, must extend `IApiFilter`.
 * @property configViewContainer The configuration object for the view container.
 */
abstract class ViewDataContainer<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    val configViewContainer: ConfigViewContainer<CC, T, ID, *, FILT>,
) : View<CC, FILT>(
    configView = configViewContainer,
) {
    companion object {
        var startTime = 0L
        val dataUpdateFuncs = HashMap<Pair<Int, String>, () -> Unit>()
        var handleInterval: Int? = null
            set(value) {
                field?.let {
                    window.clearInterval(it)
                    dataUpdateFuncs.clear()
                }
                field = value
            }

        fun clearStartTime() {
            startTime = (Date().getTime() / 1000).toLong()
        }
    }

    /**
     * Determines whether the installation of periodic updates is allowed.
     * This variable can be toggled to enable or disable periodic updates,
     * influencing the behavior of update-related operations within the system.
     */
    var allowInstallPeriodicUpdate: Boolean = true

    private var periodicUpdate = true
    abstract fun dataUpdate()

    open val onPeriodicDataUpdate: (() -> Unit)? = {
        dataUpdate()
    }

    /**
     * Displays a confirmation dialog for deleting an item and processes the delete operation
     * based on user interaction. If confirmed, it attempts to delete the specified item
     * using the provided API function and callbacks for handling success or failure scenarios.
     *
     * @param apiItemFun The API function to be used for the delete operation.
     * @param item The item to be deleted.
     * @param apiFilter An API filter instance used for filtering the delete operation. Defaults to a common filter instance.
     * @param onFail A callback invoked when the delete operation fails, providing the resulting [ItemState].
     * @param onSuccess A callback invoked when the delete operation succeeds.
     */
    fun confirmDeleteView(
        apiItemFun: Function<*>,
        item: T,
        apiFilter: FILT = configView.commonContainer.apiFilterInstance(),
        onFail: ((ItemState<T>) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
    ) {
        configView.commonContainer.callItemService(
            apiItemFun = apiItemFun,
            crudTask = CrudTask.Delete,
            callType = CallType.Query,
            id = item._id,
            item = item,
            apiFilter = apiFilter,
        ) { itemState ->
            if (itemState.hasError.not()) {
                val numSelectedRows = if (this is ViewList<*, *, *, *, *>)
                    tabulator?.getSelectedRows()?.size ?: 0 else null
                val deleteWord = gettext("Delete")
                val text = if (numSelectedRows != null && numSelectedRows > 0) {
                    "<b>$deleteWord</b> $numSelectedRows selected '<i>${configView.commonContainer.labelItem}</i>', id: <b>${
                        configView.commonContainer.labelId(item)
                    }</b> ?"
                } else "<b>$deleteWord</b> '<i>${configView.commonContainer.labelItem}</i>', id: <b>${
                    configView.commonContainer.labelId(item)
                }</b> ?"
                val modal = Confirm(
                    caption = "Please Confirm",
                    text = text,
                    rich = true,
                    size = ModalSize.XLARGE,
                    centered = true,
                    noTitle = "Cancel",
                    noCallback = {
                        Toast.warning("Delete canceled")
                    },
                    yesCallback = {
                        configView.commonContainer.callItemService(
                            apiItemFun = apiItemFun,
                            crudTask = CrudTask.Delete,
                            callType = CallType.Action,
                            id = item._id,
                            item = item,
                            apiFilter = apiFilter,
                        ) { itemState1 ->
                            if (itemState1.hasError.not()) {
                                Toast.success(
                                    message = itemState1.msgOk ?: "Delete action successful ...",
                                )
                                onSuccess?.invoke()
                            } else {
                                Toast.warning(
                                    message = itemState1.msgError ?: "Delete action failed ...",
                                )
                                onFail?.invoke(itemState1)
                            }
                            itemState1
                        }
                    }
                )
                modal.show()
            } else {
                itemState.toast()
                onFail?.invoke(itemState)
            }
            itemState
        }
    }

    fun runPeriodicBlock() {
        try {
//            console.warn("dataUpdateFuncs", dataUpdateFuncs.map { it.key }.toObj())
            dataUpdateFuncs.forEach {
//                console.warn("callBlock", it.key, it.value.toString().substringBefore("("))
                it.value.invoke()
            }
        } catch (e: Exception) {
            console.error("Error on runPeriodicBlock(): ", e)
        }
    }

    fun installUpdate() {
//        console.warn("installUpdate", this.hashCode(), this::class.simpleName, periodicUpdateDataView)
        onPeriodicDataUpdate?.let {
            dataUpdateFuncs[this.hashCode() to (this::class.simpleName ?: "?")] = it
        }
        if (handleInterval == null && periodicUpdateDataView == true) {
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    if (periodicUpdate) {
                        val curTime = (Date().getTime() / 1000).toLong()
                        if ((curTime - startTime) >= periodicUpdateViewInterval) {
                            if (!lock) {
                                startTime = curTime
                                lock = true
                                runPeriodicBlock()
                                lock = false
                            }
                        }
                    }
                },
                timeout = 250,
            )
        }
    }

    /**
     * Open function that allows to override the default action when the [apiFilterObservable] observable changes.
     * The default action will do an [updateBanner] and then an [dataUpdate]
     */
    override fun onApiFilterChange() {
        super.onApiFilterChange()
        dataUpdate()
    }

    override fun onBeforeDispose() {
        super.onBeforeDispose()
        handleInterval = null
    }

    /**
     * Resumes periodic updates by setting the `periodicUpdate` flag to `true`.
     *
     * This method is used to re-enable the periodic update mechanism within the
     * update lifecycle management in `ViewDataContainer` after it has been suspended.
     */
    @Suppress("unused")
    fun resumePeriodicUpdate() {
        periodicUpdate = true
    }

    /**
     * Suspends periodic updates by setting the `periodicUpdate` flag to `false`.
     *
     * This method temporarily disables the periodic update mechanism within
     * the update lifecycle management of `ViewDataContainer`.
     */
    @Suppress("unused")
    fun suspendPeriodicUpdate() {
        periodicUpdate = false
    }
}
