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
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json

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
 * Navigates to a specific item view using the given [navHostController].
 * The navigation is based on the provided [id] and [crudTask] parameters.
 * If [id] is not provided, it defaults to null.
 * If [crudTask] is not provided, it defaults to CrudTask.Read.
 * The [apiItemFactory] is an optional lambda function that can be used to customize the [ApiItem] instance before serializing it.
 * If not provided, the default [ApiItem] instance is used.
 *
 * @param navHostController The navigation host controller.
 * @param id The ID of the item being navigated to. Default value is null.
 * @param crudTask The CRUD task of the operation being performed. Default value is CrudTask.Read.
 * @param apiItemFactory An optional lambda function to customize the [ApiItem] instance before serializing it.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CC.navigateItem(
    navHostController: NavHostController,
    id: ID? = null,
    crudTask: CrudTask = CrudTask.Read,
    apiItemFactory: ((ApiItem<T, ID, FILT>).(FILT) -> ApiItem<T, ID, FILT>)? = null
) {
    val apiFilter = apiFilterInstance()
    val apiItem = ApiItem<T, ID, FILT>(id = id, crudTask = crudTask, apiFilter = apiFilter)
    val serializedApiItem = Json.encodeToString(
        ApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer),
        apiItemFactory?.let { it(apiItem, apiFilter) } ?: apiItem
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
