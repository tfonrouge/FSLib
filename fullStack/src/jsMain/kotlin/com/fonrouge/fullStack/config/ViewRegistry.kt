package com.fonrouge.fullStack.config

import dev.kilua.rpc.RpcServiceManager

/**
 * Registro centralizado para todas las configuraciones de vistas de la aplicación.
 *
 * Consolida los mapas de configuración de vistas (`ConfigView`, `ConfigViewItem`, `ConfigViewList`)
 * y los service managers de RPC en un único punto de acceso. Esto mejora la cohesión y facilita
 * la búsqueda de vistas por URL.
 */
object ViewRegistry {

    /**
     * Mapa de configuraciones de vista general, indexado por URL base.
     */
    val configViewMap = mutableMapOf<String, ConfigView<*, *, *>>()

    /**
     * Mapa de configuraciones de vista de item, indexado por URL base.
     */
    val configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *, *, *, *>>()

    /**
     * Mapa de configuraciones de vista de lista, indexado por URL base.
     */
    val configViewListMap = mutableMapOf<String, ConfigViewList<*, *, *, *, *, *, *>>()

    private var _itemServiceManager: RpcServiceManager<*>? = null
    private var _listServiceManager: RpcServiceManager<*>? = null

    /**
     * Service manager de RPC para operaciones de item.
     *
     * @throws IllegalStateException si se accede antes de ser inicializado.
     */
    var itemServiceManager: RpcServiceManager<*>
        get() = _itemServiceManager
            ?: error("ViewRegistry.itemServiceManager no ha sido inicializado. Asigne el valor antes de crear instancias de ConfigViewItem.")
        set(value) {
            _itemServiceManager = value
        }

    /**
     * Service manager de RPC para operaciones de lista.
     *
     * @throws IllegalStateException si se accede antes de ser inicializado.
     */
    var listServiceManager: RpcServiceManager<*>
        get() = _listServiceManager
            ?: error("ViewRegistry.listServiceManager no ha sido inicializado. Asigne el valor antes de crear instancias de ConfigViewList.")
        set(value) {
            _listServiceManager = value
        }

    /**
     * Busca una configuración de vista por su URL base, buscando en los tres mapas.
     *
     * @param baseUrl La URL base de la vista a buscar.
     * @return La configuración de vista encontrada, o null si no existe en ningún mapa.
     */
    fun findByUrl(baseUrl: String): ConfigView<*, *, *>? =
        configViewMap[baseUrl] ?: configViewItemMap[baseUrl] ?: configViewListMap[baseUrl]

    /**
     * Limpia todos los registros y service managers. Útil para pruebas.
     */
    @Suppress("unused")
    fun reset() {
        configViewMap.clear()
        configViewItemMap.clear()
        configViewListMap.clear()
        _itemServiceManager = null
        _listServiceManager = null
    }
}
