package com.fonrouge.fsLib.configCommon

import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json

val ICommonContainer<*, *, *>.routeItem: String get() = "ViewItem$name?apiItem={apiItem}"
val ICommonContainer<*, *, *>.routeList: String get() = "ViewList$name?apiFilter={apiFilter}"

@Composable
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CV.DecodeRouteItemParams(
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
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CV.DecodeRouteListParams(
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
 * Navigates to the specified view item using the provided navigation controller.
 *
 * @param navHostController The navigation controller to use for navigating.
 * @param apiItem The API item containing information about the item to navigate to.
 */
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CV.navigateItem(
    navHostController: NavHostController,
    apiItem: ApiItem<T, ID, FILT>
) {
    val serializedApiItem = Json.encodeToString(
        ApiItem.serializer(itemSerializer, idSerializer, apiFilterSerializer),
        apiItem
    )
    navHostController.navigate(
        "ViewItem$name?apiItem=\"${Uri.encode(serializedApiItem)}\""
    )
}

/**
 * Navigate to the list view with the specified API filter.
 *
 * @param navHostController the Navigation Host Controller
 * @param apiFilter the API filter (optional, default value is apiFilterInstance())
 *
 * @throws Exception if an error occurs while creating the API filter instance
 */
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> CV.navigateList(
    navHostController: NavHostController,
    apiFilter: FILT = apiFilterInstance(),
) {
    val serializedApiFilter = Json.encodeToString(apiFilterSerializer, apiFilter)
    navHostController.navigate(
        "ViewList$name?apiFilter=\"${Uri.encode(serializedApiFilter)}\""
    )
}

fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> NavGraphBuilder.composableItem(
    commonContainer: CV,
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

fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> NavGraphBuilder.composableList(
    commonContainer: CV,
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
