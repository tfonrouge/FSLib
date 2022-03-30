@file:Suppress("unused")

package com.fonrouge.fsLib.apiLib

import com.fonrouge.fsLib.ApiParam
import com.fonrouge.fsLib.apiLib.Api.API_BASE_URL
import com.fonrouge.fsLib.apiLib.Api.headers
import com.fonrouge.fsLib.apiLib.Api.restCall
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.lib.ActionParam
import com.fonrouge.fsLib.lib.KPair
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.withProgress
import com.fonrouge.fsLib.model.MediaItem
import com.fonrouge.fsLib.model.base.BaseContainer
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.*
import com.fonrouge.fsLib.view.ViewDataContainer.Companion.handleInterval
import io.kvision.jquery.JQueryAjaxSettings
import io.kvision.jquery.JQueryXHR
import io.kvision.navigo.Match
import io.kvision.redux.ReduxStore
import io.kvision.redux.createReduxStore
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.routing.Strategy
import io.kvision.routing.routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.obj
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.*
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.js.Date
import kotlin.reflect.KClass

object KVWebManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    var API_SERVER = ""

    private const val JWT_TOKEN = "jwtToken"

    lateinit var frontEndVersion: String
    lateinit var frontEndAppName: String
    var motto = "<motto>"
    var pageContainerWidth = "md"

    lateinit var kvWebStore: ReduxStore<KVWebState, KVAction>
    var configViewItemMap = mutableMapOf<String, ConfigViewItem<*, *, *>>()
    var configViewListMap = mutableMapOf<String?, ConfigViewList<*, *, *>>()
    lateinit var viewHomeBase: ViewHomeBase

    val state get() = kvWebStore.getState()

    private var authenticated = false

    const val intervalTimeout = 5
    var refreshViewItemPeriodic = false
    var refreshViewListPeriodic = false

    var setup: (KVWebManager.() -> Unit)? = null

    fun initialize() {

        setup?.invoke(this)

        kvWebStore = createReduxStore(::reducer, KVWebState())

        Routing.init(root = null, useHash = true, strategy = Strategy.ONE)
        routing.initialize().resolve()

        if (getJwtToken() != null) {
            withProgress {
                Api.loadUser()?.let {
                    kvWebStore.dispatch(IfceWebAction.Login(it))
                    saveJwtToken(it.token!!)
                    afterInitialize()
                } ?: run {
                    deleteJwtToken()
                    afterInitialize()
                }
            }
        } else {
            afterInitialize()
        }
    }

    fun authRequest(xhr: JQueryXHR, @Suppress("UNUSED_PARAMETER") settings: JQueryAjaxSettings): Boolean {
        getJwtToken()?.let {
            xhr.setRequestHeader("Authorization", "Bearer $it")
        }
        return true
    }

    internal fun getJwtToken(): String? {
        return localStorage[JWT_TOKEN]
    }

    private fun saveJwtToken(token: String) {
        authenticated = true
        localStorage[JWT_TOKEN] = token
    }

    private fun deleteJwtToken() {
        authenticated = false
        localStorage.removeItem(JWT_TOKEN)
    }

    private fun afterInitialize() {
        kvWebStore.dispatch(IfceWebAction.AppLoaded)
        if (kvWebStore.getState().view is ViewHomeBase) {
            loadHome()
        }
    }

    private fun loadHome() {
        kvWebStore.dispatch(IfceWebAction.Loaded(viewHomeBase))
        withProgress {
//            val home = Api.home()
        }
    }

    private fun parseErrors(message: String?): List<String> {
        return message?.let { it ->
            try {
                val result = mutableListOf<String>()
                val json = JSON.parse<dynamic>(it)
                val errors = json.errors
                for (key in js("Object").keys(errors)) {
                    val tab: Array<String> = errors[key] as Array<String>
                    result.addAll(tab.map { key + " " + it })
                }
                result
            } catch (e: Exception) {
                listOf("unknown error")
            }
        } ?: listOf("unknown error")
    }

    fun getMediaList(item: Pair<String, *>, context: String, f: (List<MediaItem>?) -> Unit) {
        withProgress {
            val list = restCall<List<MediaItem>>(
                url = "${API_BASE_URL}media/url/${item.first}/${item.second}/$context",
                method = HttpMethod.GET,
                headers = headers(true)
            )
            list?.let {
                f(list)
            }
        }
    }

    inline fun <reified T : BaseModel<*>> restContainerItem(
        kClass: KClass<T>,
        id: String?,
        crossinline function: ((T?) -> Unit),
    ) {
        restContainerItem(kClass.simpleName, id, function)
    }

    inline fun <reified T : BaseModel<*>> restContainerItem(
        itemClassName: String?,
        id: String?,
        crossinline function: ((T?) -> Unit),
    ) {
        configViewItemMap[itemClassName]?.let { configViewItem: ConfigViewItem<*, *, *> ->
            val data = buildJsonObject {
                configViewItem.lookupParam?.let { put("lookup", it) }
            }
            withProgress {
                id?.let {
                    restCall<T, JsonObject>(
                        url = configViewItem.restUrlTyped(TypeView.Item) + UrlParams("id" to it),
                        data = data,
                        method = HttpMethod.POST,
                        debug = true,
                        headers = headers(true)
                    ).let { t ->
                        function(t)
                    }
                } ?: function(null)
            }
        }
    }

    inline fun <reified T : BaseModel<*>> restContainerList(
        kClass: KClass<T>,
        crossinline function: ((List<T>?) -> Unit),
    ) {
        configViewListMap[kClass.simpleName]?.let { configViewList ->
            val data = buildJsonObject {
                configViewList.lookupParam?.let { put("lookup", it) }
            }
            withProgress {
                restCall<List<T>, JsonObject>(
                    url = configViewList.restUrlTyped(TypeView.List),
                    data = data,
                    method = HttpMethod.POST,
                    debug = true,
                    headers = headers(true)
                ).let { t ->
                    function(t)
                }
            }
        }
    }

    fun restCustomJsonCall(
        configViewItem: ConfigViewItem<*, *, *>,
        map: String,
        data: JsonObject,
        httpMethod: HttpMethod = HttpMethod.POST,
        function: (((JsonObject?) -> Unit))? = null,
    ) {
        withProgress {
            restCall<JsonObject, JsonObject>(
                url = configViewItem.restUrlCustom(map),
                data = data,
                method = httpMethod,
                headers = headers(true)
            ).let {
                function?.let { it1 -> it1(it) }
            }
        }
    }

    inline fun <reified T : Any, reified U : Any> restCustomCall(
        configViewItem: ConfigViewItem<*, *, *>,
        map: String,
        data: U,
        httpMethod: HttpMethod = HttpMethod.POST,
        noinline function: (((T?) -> Unit))? = null,
    ) {
        withProgress {
            restCall<T, U>(
                url = configViewItem.restUrlCustom(map),
                data = data,
                method = httpMethod,
                headers = headers(true)
            ).let {
                function?.let { it1 -> it1(it) }
            }
        }
    }

    inline fun <reified T : BaseContainer> restViewCall(
        view: View,
        lookup: JsonObject? = null,
        matchFilter: JsonObject? = null,
        sort: JsonObject? = null,
        upsertData: JsonObject? = null,
        httpMethod: HttpMethod = HttpMethod.POST,
        crossinline function: ((T?) -> Unit),
    ) {
        val data = buildJsonObject {
            (lookup ?: view.lookupParam)?.let { put("lookup", it) }
            (matchFilter ?: view.matchFilterParam)?.let { put("matchFilter", it) }
            (sort ?: view.sortParam)?.let { put("sort", it) }
            (upsertData ?: view.upsertData)?.let { put("upsertData", it) }
        }
        withProgress {
            restCall<T, JsonObject>(
                url = view.restUrl,
                data = data,
                method = httpMethod,
                debug = true,
                headers = headers(true)
            ).let {
                function(it)
            }
        }
    }

    inline fun <reified T : BaseContainer> updateViewDataContainer(
        view: ViewDataContainer<T>,
        crossinline callFunc: (ApiParam) -> T?,
        lookup: JsonObject? = null,
        matchFilter: JsonObject? = null,
        sort: JsonObject? = null,
        httpMethod: HttpMethod = HttpMethod.POST,
        noinline block: ((T?) -> Unit)? = null,
    ) {
        if (view is ViewList<*, *>) {
            view.contextClassId
        }
        var loading = if (!view.skipLoading) {
            kvWebStore.dispatch(IfceWebAction.Loading(view))
            true
        } else {
            view.skipLoading = false
            false
        }
        val callBlock: () -> Unit = {
            try {
                restViewCall<T>(
                    view = view,
                    lookup = lookup,
                    matchFilter = matchFilter,
                    sort = sort,
                    httpMethod = httpMethod
                ) {
                    view.dataContainer = it
                    if (loading) {
                        loading = false
                        kvWebStore.dispatch(IfceWebAction.Loaded(view))
                    }
                    block?.invoke(it)
                    view.displayBlock?.let { it() }
                }
            } catch (e: Exception) {
                console.warn("Error on interval =", e)
            }
        }
        if (view.repeatRefreshView == true) {
            var lastTime: Int? = null
            var lock = false
            handleInterval = window.setInterval(
                handler = {
                    val time = Date().getUTCSeconds()
                    if (lastTime != Date().getSeconds() && (time % intervalTimeout == 0)) {
                        lastTime = Date().getUTCSeconds()
                        if (!lock) {
                            lock = true
                            callBlock()
                            lock = false
                        }
                    }
                },
                timeout = 250
            )
        }
        callBlock()
    }

    fun <T : BaseModel<*>> updateItem(
        item: T,
        vararg fieldSet: KPair<T, *>,
        block: ((Boolean?) -> Unit)? = null,
    ) {

        val upsertCmd = ArrayList<dynamic>()
        val query = obj { }
        query["id"] = item.id
        upsertCmd.add(query)
        val update = obj { }
        fieldSet.forEach { kPair2 ->
            update[kPair2.kProp.name] = kPair2.value
        }
        configViewItemMap[item::class.simpleName]?.let { itemConfigView ->
            callUpsert(
                upsertCmd = upsertCmd,
                action = ActionParam.Update.name,
                update = update,
                origItem = item,
                configViewItem = itemConfigView,
                block = block
            )
        }
    }

    fun callUpsert(
        upsertCmd: ArrayList<dynamic>,
        action: String,
        update: dynamic,
        origItem: dynamic,
        configViewItem: ConfigViewItem<*, *, *>,
        block: ((Boolean?) -> Unit)? = null,
    ) {
        val o = obj { }
        o["\$set"] = update
        o["origItem"] = origItem
        upsertCmd.add(o)
        val jsonObject = buildJsonObject {
            put("action", action)
            put(
                key = "upsert",
                element = Json.parseToJsonElement(js("JSON.stringify(upsertCmd)") as String)
            )
/*
            val upsertInfo = UpsertInfo(
                userId = state.userLogged?.id ?: "?",
                userName = state.userLogged?.fullName ?: "?",
                userKey = state.userLogged?.userId ?: "?",
                date = Date().toISOString(),
                type = action
            )
            val j = Json.encodeToJsonElement(upsertInfo)
            put("upsertInfo", j)
*/
        }
        withProgress {
            restCall<Boolean, JsonElement>(
                url = configViewItem.restUrlTyped(TypeView.Upsert),
                data = jsonObject,
                method = HttpMethod.POST,
                headers = headers(true),
                block = block
            )
        }
    }

    fun upsertItem(
        viewItem: ViewItem<*, *>,
        customUpdate: dynamic = null,
        block: ((Boolean?) -> Unit)? = null,
    ) {
        viewItem.urlParams?.action?.name?.let { action ->
            val upsertCmd = ArrayList<dynamic>()
            val query = obj { }
            if (action == ActionParam.Update.name) {
                query["id"] = viewItem.origObjItem["id"]
            }
            upsertCmd.add(query)
            val update = if (customUpdate == null) {
                val update1 = obj { }
                viewItem.formPanel?.let { itemFormPanel: ItemFormPanel<out BaseModel<*>> ->
                    itemFormPanel.form.fields.forEach { entry ->
                        val name = entry.key.asDynamic()
                        val origValue = viewItem.origObjItem[name]
                        val curValue = entry.value.getValue()
                        console.warn("comparing ", origValue, curValue)
                        val v = if (origValue is Date || curValue is Date) {
                            val d1 = (origValue as? Date)?.toISOString()
                            val d2 = (curValue as? Date)?.toISOString()
                            if (d1 != d2) {
                                val o = obj { }
                                o["\$date"] = d2
                                o
                            } else null
                        } else if (origValue is BaseModel<*>) {
                            val o1 = origValue as? BaseModel<*>
                            if (o1?.id != curValue) {
                                curValue
                            } else null
                        } else {
                            if (curValue != origValue) curValue else null
                        }
                        if (v != null) {
                            console.warn(name, ":", "it =", v, "->", "'${curValue}'", " != ", "'${origValue}'")
                            update1[name] = v
                        }
                    }
                }
                if (action == ActionParam.Insert.name) {
                    viewItem.urlParams?.contextClassId?.let { contextClassId ->
                        val contextName = contextClassId.contextName
                        if (contextName != null && update1[contextName] == null) {
                            update1[contextName] = contextClassId.contextId
                        }
                    }
                }
                viewItem.beforeUpdate(update1)
                update1
            } else {
                customUpdate
            }
            if (update == null) {
                Toast.warning(
                    message = "Update tried without changed fields...",
                    options = ToastOptions(
                        positionClass = ToastPosition.BOTTOMFULLWIDTH
                    )
                )
                block?.let { it(false) }
                return
            }
            callUpsert(
                upsertCmd = upsertCmd,
                action = action,
                update = update,
                origItem = viewItem.origObjItem,
                configViewItem = viewItem.configViewItem,
                block = block
            )
        }
    }

    fun <T : BaseModel<*>> deleteItem(item: T, block: ((Boolean?) -> Unit)? = null) {
        configViewItemMap[item::class.simpleName]?.let { configViewItem: ConfigViewItem<*, *, *> ->
            withProgress {
                restCall<Boolean>(
                    url = configViewItem.restUrlTyped(TypeView.Item) + UrlParams(
                        "id" to item.id,
                        "action" to ActionParam.Delete.name
                    ),
                    headers = headers(true),
                    method = HttpMethod.DELETE
                ).let {
                    block?.invoke(it)
                }
            }
        }
    }

    fun login(userName: String?, password: String?) {
        withProgress {
            deleteJwtToken()
            Api.login(userName, password)?.let { user ->
                kvWebStore.dispatch(IfceWebAction.Login(user))
                saveJwtToken(user.token!!)
                routing.navigate("")
                val lastLogin = obj {}
                lastLogin["lastLogin"] = Date()
            } ?: kvWebStore.dispatch(IfceWebAction.LoginError(parseErrors("login error message")))
        }
    }

    fun logout() {
        deleteJwtToken()
        kvWebStore.dispatch(IfceWebAction.Logout)
        routing.navigate("logout")
    }

    fun userItem(image: String?, nombreCorto: String?, nombre: String?, email: String?, password: String?) {
        withProgress {
            val user = Api.settings(image, nombreCorto, nombre, email, password)
            if (user != null) {
                kvWebStore.dispatch(IfceWebAction.Login(user))
                saveJwtToken(user.token!!)
//                routing.navigate("${ViewUserItem.url}${user.id}")
            } else {
                kvWebStore.dispatch(IfceWebAction.UserItemError(parseErrors("another user item e.message")))
            }
        }
    }

    fun runLoginPage() {
        kvWebStore.dispatch(IfceWebAction.LoginPage)
    }

    internal fun showToastApiRemoteRequest(code: Int, title: String, message: String) {

        val msgDetail = if (code == 403) {
//            handleInterval = null
            authenticated = false
            """
<p>-<p>
The resource requires authentication which was not supplied with the request<br>
<b>Please Login
"""
        } else {
            ""
        }
        showToastApiError(title, message + msgDetail, 10000) {
            if (!authenticated) {
                routing.navigate("login")
            }
        }
    }

    fun showToastApiError(title: String, message: String, timeOut: Int = 5000, onHidden: (() -> Unit)? = null) {
        Toast.error(
            message = message,
            title = title,
            options = ToastOptions(
                positionClass = ToastPosition.BOTTOMFULLWIDTH,
                progressBar = true,
                closeButton = true,
                timeOut = timeOut,
                onHidden = onHidden,
            ),
        )
    }

    fun dispatchViewListPage(
        configViewList: ConfigViewList<*, *, *>,
        match: Match
    ) {
        configViewList.viewFunc?.let { it(UrlParams(match)) }?.let { viewList ->
            viewList.dispatchActionPage()
            console.warn("viewList.dispatchActionPage()")
            configViewList.updateData(UrlParams(match = match))
        }
    }
}
