package com.fonrouge.fsLib.configCommon

import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.CrudTask
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.apiData.serializeMasterItemId
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlin.reflect.KSuspendFunction1

val ICommonContainer<*, *, *>.routeItem: String get() = "ViewItem$name?apiItem={apiItem}"
val ICommonContainer<*, *, *>.routeList: String get() = "ViewList$name?apiFilter={apiFilter}"

@Composable
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.DecodeRouteItemParams(
    navBackStackEntry: NavBackStackEntry,
    function: @Composable (apiItem: ApiItem<T, ID, FILT>) -> Unit
) {
    val apiItem = navBackStackEntry.arguments?.getString("apiItem")?.let {
        if (it != "\"null\"") Json.decodeFromString(
            ApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer),
            it.removePrefix("\"").removeSuffix("\"")
        ) else null
    }
    apiItem?.let { function(it) }
}

@Composable
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.DecodeRouteListParams(
    navBackStackEntry: NavBackStackEntry,
    function: @Composable (apiFilter: FILT) -> Unit
) {
    val serializedApiFilter =
        navBackStackEntry.arguments?.getString("apiFilter")?.removePrefix("\"")?.removeSuffix("\"")
    function(
        serializedApiFilter?.let {
            Json.decodeFromString(
                apiFilterSerializer.nullable,
                it
            )
        } ?: apiFilterInstance()
    )
}

/**
 * Navigates to the specified item in the navigation host controller using the provided parameters.
 *
 * @param navHostController The navigation host controller to navigate.
 * @param id The optional ID of the item to navigate to.
 * @param item The optional item to navigate to.
 * @param callType The API call type. Defaults to [ApiItem.CallType.Query].
 * @param crudTask The CRUD task. Defaults to [CrudTask.Read].
 * @param apiFilter The API filter. Defaults to an instance returned by [apiFilterInstance].
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.navigateItem(
    navHostController: NavHostController,
    id: ID? = null,
    item: T? = null,
    callType: ApiItem.CallType = ApiItem.CallType.Query,
    crudTask: CrudTask = CrudTask.Read,
    apiFilter: FILT = apiFilterInstance()
) {
    val serializedApiItem = Json.encodeToString(
        ApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer),
        ApiItem(
            id = id,
            item = item,
            callType = callType,
            crudTask = crudTask,
            apiFilter = apiFilter
        )
    )
    navHostController.navigate(
        "ViewItem$name?apiItem=\"${Uri.encode(serializedApiItem)}\""
    )
}

/**
 * Navigates to a child list view with the specified API filter.
 *
 * @param navHostController the Navigation Host Controller
 * @param masterItem the master item to serialize its ID for the API filter
 * @throws Exception if an error occurs while creating the API filter instance
 */
@Suppress("unused")
inline fun <MI : BaseDoc<MID>, reified MID : Any, CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.navigateChildList(
    navHostController: NavHostController,
    masterItem: MI?,
) {
    return navigateList(
        navHostController = navHostController,
        apiFilterFactory = {
            it.serializeMasterItemId(masterItem?._id)
        }
    )
}

/**
 * Navigate to the list view with the specified API filter.
 *
 * @param navHostController the Navigation Host Controller
 * @param apiFilterFactory to allow to refactor the apiFilter instance
 *
 * @throws Exception if an error occurs while creating the API filter instance
 */
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.navigateList(
    navHostController: NavHostController,
    apiFilterFactory: ((FILT) -> FILT)? = null,
) {
    val apiFilter: FILT = apiFilterFactory?.let { it(apiFilterInstance()) } ?: apiFilterInstance()
    val serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    navHostController.navigate(
        "ViewList$name?apiFilter=\"${Uri.encode(serializedApiFilter)}\""
    )
}

/**
 * Calls the item API using the provided function.
 *
 * @param function the suspend function that takes an [ApiItem] and returns an [ItemState]
 * @param id the ID of the item
 * @param item the item object
 * @param callType the type of API call, default is [ApiItem.CallType.Query]
 * @param crudTask the CRUD task, default is [CrudTask.Read]
 * @param apiFilter the API filter object, default is the instance created by [apiFilterInstance]
 * @param onResponse optional callback function to handle the [ItemState] response
 * @return the [ItemState] result of the function call
 */
@Suppress("unused")
suspend fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.callItemApi(
    function: KSuspendFunction1<ApiItem<T, ID, FILT>, ItemState<T>>,
    id: ID? = null,
    item: T? = null,
    callType: ApiItem.CallType = ApiItem.CallType.Query,
    crudTask: CrudTask = CrudTask.Read,
    apiFilter: FILT = apiFilterInstance(),
    onResponse: (CC.(ItemState<T>) -> Unit)? = null,
): ItemState<T> {
    val itemState = function(
        ApiItem(
            id = id,
            item = item,
            callType = callType,
            crudTask = crudTask,
            apiFilter = apiFilter
        )
    )
    onResponse?.let { it(itemState) }
    return itemState
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> NavGraphBuilder.composableItem(
    commonContainer: CC,
    function: @Composable AnimatedContentScope.(ApiItem<T, ID, FILT>) -> Unit,
) {
    composable(commonContainer.routeItem) { navBackStackEntry ->
        commonContainer.DecodeRouteItemParams(
            navBackStackEntry = navBackStackEntry,
        ) {
            function(it)
        }
    }
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> NavGraphBuilder.composableList(
    commonContainer: CC,
    function: @Composable AnimatedContentScope.(FILT) -> Unit,
) {
    composable(commonContainer.routeList) { navBackStackEntry ->
        commonContainer.DecodeRouteListParams(
            navBackStackEntry = navBackStackEntry,
        ) {
            function(it)
        }
    }
}
