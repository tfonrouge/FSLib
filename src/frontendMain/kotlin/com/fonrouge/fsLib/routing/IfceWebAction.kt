package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.model.UserLogged
import com.fonrouge.fsLib.view.View
import com.fonrouge.fsLib.view.ViewHome

open class IfceWebAction : KVAction() {
    data class Loading(val view: View) : IfceWebAction()
    data class Loaded(val view: View) : IfceWebAction()
    object AppLoaded : IfceWebAction()
    class HomePage(view: ViewHome) : IfceWebAction()
    object LoginPage : IfceWebAction()
    class Login(val userLogged: UserLogged) : IfceWebAction()
    data class LoginError(val errors: List<String>) : IfceWebAction()
    object Logout : IfceWebAction()
    data class UserItemError(val errors: List<String>) : IfceWebAction()
}
