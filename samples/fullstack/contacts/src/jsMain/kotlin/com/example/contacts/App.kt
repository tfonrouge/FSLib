package com.example.contacts

import dev.kilua.rpc.getService
import io.kvision.*
import io.kvision.core.onClickLaunch
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.h4
import io.kvision.navbar.navbar
import io.kvision.panel.root
import io.kvision.panel.vPanel
import io.kvision.remote.registerRemoteTypes
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Layout
import io.kvision.tabulator.PaginationMode
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import io.kvision.toast.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Application-scoped coroutine scope. */
val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

/**
 * KVision application demonstrating a Tabulator data grid
 * with data loaded via RPC from the server.
 */
class App : Application() {

    private val contactService = getService<IContactService>()

    override fun start(state: Map<String, Any>) {
        root("kvapp") {
            navbar(label = "Contacts Sample", collapseOnClick = true)

            vPanel(className = "container-fluid mt-3") {
                h4("Contact List")

                val tbl = tabulator(
                    options = TabulatorOptions(
                        layout = Layout.FITCOLUMNS,
                        pagination = true,
                        paginationMode = PaginationMode.LOCAL,
                        paginationSize = 10,
                        columns = listOf(
                            ColumnDefinition(title = "First Name", field = "firstName"),
                            ColumnDefinition(title = "Last Name", field = "lastName"),
                            ColumnDefinition(title = "Email", field = "email"),
                            ColumnDefinition(title = "Phone", field = "phone"),
                            ColumnDefinition(title = "Company", field = "company"),
                            ColumnDefinition(title = "Role", field = "role"),
                        ),
                    ),
                    serializer = Contact.serializer(),
                )

                // Load data on startup
                AppScope.launch {
                    val result = contactService.listContacts()
                    tbl.setData(result.data.toTypedArray())
                }

                button("Refresh", style = ButtonStyle.OUTLINESECONDARY, className = "mt-2") {
                    onClickLaunch {
                        val result = contactService.listContacts()
                        tbl.setData(result.data.toTypedArray())
                        Toast.info("Loaded ${result.total} contacts")
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
        TomSelectModule,
        TabulatorModule,
        TabulatorCssBootstrapModule,
        MaterialModule,
        CoreModule,
    )
}
