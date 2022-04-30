package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.apiLib.KVWebManager.viewHomeBase
import com.fonrouge.fsLib.config.ConfigViewContainer
import com.fonrouge.fsLib.model.UserLogged
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewHomeBase
import io.kvision.redux.RAction

open class KVWebState(

    /* state */
    var errorMessage: String? = null,
    var loginErrors: List<String>? = null,
    var userItemErrors: List<String>? = null,
    var view: View = viewHomeBase,

    /* loading */
    var appLoading: Boolean = false,
) {
    fun copy(block: KVWebState.() -> Unit): KVWebState {
        block(this)
        return this
    }
}

abstract class KVAction : RAction

fun reducer(state: KVWebState, kvAction: KVAction): KVWebState {
    return when (kvAction) {
        is IfceWebAction.Loading -> state.apply { view.loading = true }
        is IfceWebAction.Loaded -> state.apply { view.loading = false }
        is IfceWebAction.AppLoaded -> state.copy { appLoading = false }
        is IfceWebAction.Logout -> KVWebState(appLoading = false)
        is ConfigViewContainer<*, *> -> {
            kvAction.viewFunc.invoke(null).let { viewDataContainer ->
                state.copy { view = viewDataContainer }
            }
        }
        else -> KVWebState()
    }
}

open class IfceWebAction : KVAction() {
    data class Loading(val view: View) : IfceWebAction()
    data class Loaded(val view: View) : IfceWebAction()
    object AppLoaded : IfceWebAction()
    class HomePage(view: ViewHomeBase) : IfceWebAction()
    object LoginPage : IfceWebAction()
    class Login(val userLogged: UserLogged) : IfceWebAction()
    data class LoginError(val errors: List<String>) : IfceWebAction()
    object Logout : IfceWebAction()
    data class UserItemError(val errors: List<String>) : IfceWebAction()
}
