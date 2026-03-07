package com.fonrouge.fullStack.routing

import com.fonrouge.base.lib.UrlParams
import com.fonrouge.fullStack.config.ViewRegistry
import com.fonrouge.fullStack.view.KVWebManager.viewStateObservableValue
import com.fonrouge.fullStack.view.ViewState
import io.kvision.navigo.Navigo

/**
 * Inicializa el router Navigo con comportamientos de ruta predefinidos, estableciendo estados
 * de vista basados en configuraciones y parámetros de URL.
 *
 * Configura dos rutas:
 * - Una ruta por defecto (path vacío) que busca la vista en [ViewRegistry].
 * - Una ruta dinámica `:viewClass` que busca la vista correspondiente en [ViewRegistry].
 *
 * Si no se encuentra configuración para el path vacío, se muestra una advertencia en consola.
 *
 * @return La instancia inicializada de Navigo para encadenar llamadas.
 */
fun Navigo.initialize(): Navigo {
    return this
        .on({
            ViewRegistry.findByUrl("")?.let {
                viewStateObservableValue.value = ViewState(it, UrlParams())
            } ?: run {
                console.warn("no configView defined to empty path")
            }
        })
        .on(
            path = ":viewClass",
            f = { match ->
                val route = match.data.viewClass as? String ?: return@on
                ViewRegistry.findByUrl(route)?.let { configView ->
                    viewStateObservableValue.value = ViewState(configView, UrlParams(match))
                }
            }
        )
}
