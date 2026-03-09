package com.example.greeting

import dev.kilua.rpc.getService
import io.kvision.*
import io.kvision.core.AlignItems
import io.kvision.core.JustifyContent
import io.kvision.core.onClickLaunch
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.navbar.navbar
import io.kvision.panel.hPanel
import io.kvision.panel.root
import io.kvision.panel.vPanel
import io.kvision.remote.registerRemoteTypes
import io.kvision.toast.Toast

/**
 * Minimal KVision application demonstrating RPC communication.
 * A text input sends a name to the server and displays the greeting response.
 */
class App : Application() {

    private val greetingService = getService<IGreetingService>()

    override fun start(state: Map<String, Any>) {
        root("kvapp") {
            navbar(label = "Greeting Sample", collapseOnClick = true)

            vPanel(
                justify = JustifyContent.CENTER,
                alignItems = AlignItems.CENTER,
                className = "mt-5",
            ) {
                h4("Enter your name to get a greeting from the server:")

                val nameInput = text(label = "Your name") {
                    placeholder = "Type your name..."
                    width = io.kvision.core.CssSize(50, io.kvision.core.UNIT.perc)
                }

                hPanel(spacing = 10, className = "mt-3") {
                    button("Greet me!", style = ButtonStyle.PRIMARY) {
                        onClickLaunch {
                            val name = nameInput.value ?: ""
                            val response = greetingService.greet(name)
                            Toast.info(response)
                        }
                    }
                    button("Server time", style = ButtonStyle.OUTLINESECONDARY) {
                        onClickLaunch {
                            val time = greetingService.serverTime()
                            Toast.info("Server time: $time")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Application entry point.
 */
fun main() {
    registerRemoteTypes()
    startApplication(
        ::App,
        js("import.meta.webpackHot").unsafeCast<Hot?>(),
        BootstrapModule,
        BootstrapCssModule,
        ToastifyModule,
        FontAwesomeModule,
        TabulatorModule,
        TabulatorCssBootstrapModule,
        MaterialModule,
        CoreModule,
    )
}
