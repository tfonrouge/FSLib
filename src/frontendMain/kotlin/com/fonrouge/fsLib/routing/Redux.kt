package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.config.ConfigView
import com.fonrouge.fsLib.config.ConfigViewContainer

fun reducer(state: KVWebState, kvAction: KVAction): KVWebState {
    console.warn("reducer with", state, kvAction)
    return when (kvAction) {
        is IfceWebAction.Loading -> state.apply { view?.loading = true }
        is IfceWebAction.Loaded -> state.apply { view?.loading = false }
        is IfceWebAction.AppLoaded -> state.copy { appLoading = false }
        is IfceWebAction.Logout -> KVWebState(appLoading = false)
        is ConfigView<*> -> {
            console.warn("reducer using ConfigView<*>")
            kvAction.viewFunc.invoke(null).let { viewDataContainer ->
                state.copy { view = viewDataContainer }
            }
        }
        is ConfigViewContainer<*, *> -> {
            console.warn("reducer using ConfigViewContainer<*>")
            kvAction.viewFunc.invoke(null).let { viewDataContainer ->
                state.copy { view = viewDataContainer }
            }
        }
        else -> {
            console.warn("reducer ELSE")
            KVWebState()
        }
    }
}
