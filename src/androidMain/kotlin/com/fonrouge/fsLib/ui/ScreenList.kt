package com.fonrouge.fsLib.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.viewModel.ViewModelList

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified VML : ViewModelList<CC, T, ID, FILT>, CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> ScreenList(
    navHostController: NavHostController = NavHostController(LocalContext.current),
    viewModel: VML = viewModel(),
    noinline topBarTitle: @Composable () -> Unit = {},
    noinline topBarNavigationIcon: @Composable () -> Unit = {
        IconButton(onClick = { navHostController.popBackStack() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    },
    noinline topBarActions: @Composable RowScope.() -> Unit = {},
    noinline bottomBar: @Composable () -> Unit = {},
    noinline floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    noinline modalNavigationDrawerContent: @Composable ColumnScope.(DrawerState) -> Unit = {},
    drawerState: DrawerState,
    noinline bodyListContent: @Composable (T?) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = topBarTitle,
                navigationIcon = topBarNavigationIcon,
                actions = topBarActions
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState(viewModel)) },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
    ) { paddingValues ->
        ScreenStateAlert(viewModelBase = viewModel)
        ScreenConfirmAlert(viewModelBase = viewModel)
        ModalNavigationDrawer(
            modifier = Modifier.padding(paddingValues),
            drawerState = drawerState,
            drawerContent = {
                ScreenFilter1(
                    drawerState = drawerState,
                    content = modalNavigationDrawerContent
                )
            }
        ) {
            BodyList(
                viewModel = viewModel,
                pullRefreshState = pullRefreshState(viewModel = viewModel),
                content = bodyListContent
            )
        }
    }
}

@Preview
@Composable
fun ScreenFilter1(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    content: @Composable ColumnScope.(DrawerState) -> Unit = {}
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .padding(top = 10.dp),
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = { content(drawerState) }
        )
    }
}
