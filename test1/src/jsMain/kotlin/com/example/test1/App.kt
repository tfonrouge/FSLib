package com.example.test1

import io.kvision.*
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
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
            tabulatorTest()
        }
    }
}

fun main() {
    registerRemoteTypes()
    startApplication(
        ::App,
        js("import.meta.webpackHot").unsafeCast<Hot?>(),
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
