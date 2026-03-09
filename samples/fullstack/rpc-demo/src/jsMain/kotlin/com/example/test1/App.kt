package com.example.test1

import com.fonrouge.fullStack.layout.dropEnd
import com.fonrouge.fullStack.layout.dropItem
import com.fonrouge.fullStack.layout.separator
import io.kvision.*
import io.kvision.core.onClick
import io.kvision.dropdown.AutoClose
import io.kvision.dropdown.dropDown
import io.kvision.html.*
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
import io.kvision.navbar.navbar
import io.kvision.panel.root
import io.kvision.remote.registerRemoteTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@JsModule("/kotlin/modules/i18n/messages-en.json")
external val messagesEn: dynamic

@JsModule("/kotlin/modules/i18n/messages-pl.json")
external val messagesPl: dynamic

class App : Application() {

    override fun start(state: Map<String, Any>) {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "en" to messagesEn,
                    "pl" to messagesPl
                )
            )

        root("kvapp") {
            navbar(collapseOnClick = true) {
                dropDown(text = "Option 0", forNavbar = true) {
                    dropEnd(
                        text = "Sub menu 0",
                    ) {
                        dropItem(text = "Menu item 0 1")
                        dropItem(text = "Menu item 0 2")
                        separator()
                        dropItem(text = "Menu item 0 3") {
                            onClick {
                                console.warn("click 0")
                            }
                        }
                    }
                }
                dropDown(text = "Option 1", forNavbar = true, autoClose = AutoClose.OUTSIDE) {
//                    dropEnd(
//                        text = "Sub menu 1",
//                        dropEnd(
//                            text = "Menu item A",
//                            dropItem(text = "Menu item A 1"),
//                            dropItem(text = "Menu item A 2") {
//                                onClick {
//                                    console.warn("click 1")
//                                }
//                            },
//                        ),
//                        dropItem(text = "Menu item B"),
//                        dropItem(text = "Menu item C"),
//                        dropEnd(
//                            text = "Menu item D",
//                            dropItem(text = "Menu item D 1"),
//                            dropItem(text = "Menu item D 2"),
//                        ),
//                    )
                }
                dropDown(text = "Option 2", forNavbar = true, autoClose = AutoClose.OUTSIDE) {
                    div(className = "dropend") {
                        tag(TAG.A, content = "Sub menu 1", className = "dropdown-item dropdown-toggle") {
                            setAttribute("data-bs-toggle", "dropdown")
                            setAttribute("data-bs-auto-close", "outside")
                        }
                        ul(className = "dropdown-menu dropdown-menu-end") {
                            li(content = "Menu item", className = "dropdown-item")
                            li(content = "Menu item", className = "dropdown-item")
                            li(content = "Menu item", className = "dropdown-item")
                            div(className = "dropend") {
                                tag(TAG.A, content = "Sub menu 1", className = "dropdown-item dropdown-toggle") {
                                    setAttribute("data-bs-toggle", "dropdown")
                                    setAttribute("data-bs-auto-close", "outside")
                                }
                                ul(className = "dropdown-menu dropdown-menu-end") {
                                    li(content = "Menu item", className = "dropdown-item") {
                                        onClick {
                                            console.warn("click 1")
//                                            this@dropDown.toggle()
                                            toggle()
                                        }
                                    }
                                    li(content = "Menu item", className = "dropdown-item")
                                    li(content = "Menu item", className = "dropdown-item")
                                }
                            }
                        }
                    }
                    div(className = "dropend") {
                        tag(TAG.A, content = "Sub menu 2", className = "dropdown-item dropdown-toggle") {
                            setAttribute("data-bs-toggle", "dropdown")
                            setAttribute("data-bs-auto-close", "outside")
                        }
                        ul(className = "dropdown-menu dropdown-menu-end") {
                            li(content = "Menu item 2", className = "dropdown-item")
                            li(content = "Menu item 2", className = "dropdown-item")
                            li(content = "Menu item 2", className = "dropdown-item")
                        }
                    }
                }
            }
        }
    }
}

/*
                dropDown(text = "Option 1", forNavbar = true) {
                    dropDown(text = "1st Sub menu", forDropDown = true, autoClose = AutoClose.OUTSIDE) {
                        ddLink("Menu item")
                        ddLink("Menu item")
                        dropDown(text = "2nd Dropdown", forDropDown = true) {
                            ddLink("Menu item")
                            ddLink("Menu item")
                            ddLink("Menu item")
                        }
                    }
                    separator()
                    dropDown(text = "2nd Sub menu", forDropDown = true, autoClose = AutoClose.OUTSIDE) {
                        onEventLaunch("show.bs.dropdown") {
                            console.warn("change 2")
                        }
                        ddLink("Sub menu 2.1")
                        ddLink("Sub menu 2.2")
                        ddLink("Sub menu 2.3")
                    }
                }

 */
fun main() {
    registerRemoteTypes()
    startApplication(
        ::App,
        js("import.meta.webpackHot").unsafeCast<Hot?>(),
        BootstrapModule,
        BootstrapCssModule,
        DatetimeModule,
        RichTextModule,
        TomSelectModule,
        ImaskModule,
        ToastifyModule,
        FontAwesomeModule,
        BootstrapIconsModule,
        PrintModule,
        ChartModule,
        TabulatorModule,
        TabulatorCssBootstrapModule,
        MaterialModule,
        CoreModule
    )
}
