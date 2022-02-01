package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.IfceWebAction
import com.fonrouge.fslib.apiLib.KVWebManager
import com.fonrouge.fslib.apiLib.TypeView
import com.fonrouge.fslib.config.BaseConfigView
import com.fonrouge.fslib.lib.UrlParams
import com.fonrouge.fslib.view.ViewDataContainer.Companion.clearHandleIntervalStack
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.form.text.TextInput
import io.kvision.form.text.TextInputType
import io.kvision.form.text.textInput
import io.kvision.html.*

object ViewLogin : View(
    configView = BaseConfigView(name = "login", label = "Login", typeView = TypeView.None),
    actionPage = { IfceWebAction.LoginPage },
    modal = true
) {
    override var urlParams: UrlParams? = null

    override fun displayPage(container: Container) {
        container.apply {

            clearHandleIntervalStack()

            div(className = "auth-page") {
                div(className = "container page") {
                    div(className = "row") {
                        div(className = "col-md-6 offset-md-3 col-xs-12") {
                            h1("Login", className = "text-xs-center")
                            p(className = "text-xs-center") {
//                        link("Need an account?", "${View.REGISTER.url}")
                            }
                            if (!KVWebManager.state.loginErrors.isNullOrEmpty()) {
                                ul(KVWebManager.state.loginErrors, className = "error-messages")
                            }
                            lateinit var userNameInput: TextInput
                            lateinit var passwordInput: TextInput
                            form {
                                fieldset(className = "form-group") {
                                    userNameInput =
                                        textInput(
                                            type = TextInputType.TEXT,
                                            className = "form-control form-control-lg"
                                        ) {
                                            placeholder = "Nombre de usuario"
                                        }
                                }
                                fieldset(className = "form-group") {
                                    passwordInput =
                                        textInput(TextInputType.PASSWORD, className = "form-control form-control-lg") {
                                            placeholder = "Password"
                                        }
                                }
                                button(
                                    "Login",
                                    type = ButtonType.SUBMIT,
                                    className = "btn-lg pull-xs-right"
                                )
                            }.onEvent {
                                submit = { ev ->
                                    ev.preventDefault()
                                    KVWebManager.login(userNameInput.value, passwordInput.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
