package com.example.showcase

import com.fonrouge.fullStack.config.registerEntityViews
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
 * - ViewHome as a non-data landing page (ICommon, no data model)
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

        // Register all views via the DSL
        val reg = registerEntityViews(getServiceManager<ITaskService>()) {
            // Non-data view: landing page using ICommon (no data model)
            view(ViewHome.configViewHome, isDefault = true)
            // Data-bound views: uses companion-object configs with ICommonContainer
            list(ViewListTask.configViewList)
            item(ViewItemTask.configViewItem)
        }

        // Register the help documentation service
        HelpDocsServiceRegistry.service = getService<IShowcaseHelpDocsService>()

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
            defaultView = reg.defaultView
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
