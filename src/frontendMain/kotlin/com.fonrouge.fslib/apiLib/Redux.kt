package com.fonrouge.fslib.apiLib

import com.fonrouge.fslib.apiLib.KVWebManager.viewHomeBase
import com.fonrouge.fslib.model.UserLogged
import com.fonrouge.fslib.view.View
import com.fonrouge.fslib.view.ViewHomeBase
import com.fonrouge.fslib.view.ViewList
import io.kvision.redux.RAction

class KVWebState(

    /* state */
    var errorMessage: String? = null,
    var loginErrors: List<String>? = null,
    var userLogged: UserLogged? = null,
    var userItemErrors: List<String>? = null,
    var view: View = viewHomeBase,

    /* loading */
    var appLoading: Boolean = true,
) {
    fun copy(block: KVWebState.() -> Unit): KVWebState {
        block(this)
        return this
    }
}

open class IfceWebAction : RAction {
    data class Loading(val view: View) : IfceWebAction()
    data class Loaded(val view: View) : IfceWebAction()
    object AppLoaded : IfceWebAction()
    data class HomePage(val view: ViewHomeBase) : IfceWebAction()
    object LoginPage : IfceWebAction()
    data class Login(val userLogged: UserLogged) : IfceWebAction()
    data class LoginError(val errors: List<String>) : IfceWebAction()
    object Logout : IfceWebAction()
    data class UserItemError(val errors: List<String>) : IfceWebAction()
    data class updateContainerList(val viewList: ViewList<*, *>) : IfceWebAction()
}
