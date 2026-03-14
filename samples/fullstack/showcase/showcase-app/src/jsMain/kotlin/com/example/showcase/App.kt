package com.example.showcase

import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.config.ConfigViewList
import com.fonrouge.fullStack.config.ViewRegistry
import com.fonrouge.fullStack.services.HelpDocsServiceRegistry
import com.fonrouge.fullStack.view.KVWebManager
import com.fonrouge.fullStack.view.showView
import dev.kilua.rpc.getService
import dev.kilua.rpc.getServiceManager
import io.kvision.*
import io.kvision.panel.root
import io.kvision.panel.vPanel
import io.kvision.remote.registerRemoteTypes
import io.kvision.state.bind

/**
 * KVision application demonstrating FSLib's full view system:
 * - ViewList with Tabulator grid (pagination, filtering, sorting)
 * - ViewItem with form panel (create, read, update)
 * - ConfigViewList / ConfigViewItem declarative configuration
 * - ViewRegistry for centralized view registration
 * - formRow / formColumn layout helpers
 * - Toolbar with CRUD buttons
 * - Toast notifications
 */
class App : Application() {

    override fun start(state: Map<String, Any>) {

        // Register the RPC service managers with the ViewRegistry
        val serviceManager = getServiceManager<ITaskService>()
        ViewRegistry.itemServiceManager = serviceManager
        ViewRegistry.listServiceManager = serviceManager

        // Register the help documentation service
        HelpDocsServiceRegistry.service = getService<IShowcaseHelpDocsService>()

        // Force-reference the companion objects so ConfigView registrations execute
        ViewListTask.configViewList
        ViewItemTask.configViewItem

        root("kvapp") {
            vPanel {
                bind(KVWebManager.viewStateObservableValue) { viewState ->
                    viewState?.let { showView(it) }
                }
            }
        }

        // Initialize routing — navigates to the default view
        KVWebManager.initialize {
            frontEndAppName = "FSLib Showcase"
            frontEndVersion = "1.0.0"
            defaultView = ViewListTask.configViewList
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
