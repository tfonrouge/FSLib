package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlin.js.Date

/**
 * An abstract class `ViewDataContainer` which extends from the `View`. This class is designed
 * to manage the configuration and periodic update of a view container.
 *
 * @param CC The type of the common container, must extend from `ICommonContainer`.
 * @param T The type of the data item, must extend from `BaseDoc`.
 * @param ID The type of the ID of data item, which must be a non-nullable type.
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

    fun installUpdate() {
//        console.warn("installUpdate", this.hashCode(), this::class.simpleName, periodicUpdateDataView)
        onPeriodicDataUpdate?.let {
            dataUpdateFuncs[this.hashCode() to (this::class.simpleName ?: "?")] = it
        }

        fun runPeriodicBlock() = {
            try {
                AppScope.launch {
//                    console.warn("dataUpdateFuncs", dataUpdateFuncs.map { it.key }.toObj())
                    dataUpdateFuncs.forEach {
//                        console.warn("callBlock", it.key, it.value.toString().substringBefore("("))
                        launch { it.value.invoke() }
                    }
                }
            } catch (e: Exception) {
                console.error("Error on interval =", e)
            }
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
     * open function that allows to override the default action when the [apiFilterObservable] observable changes.
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
