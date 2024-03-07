package com.fonrouge.fsLib.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fonrouge.fsLib.viewModel.ViewModelItem

@Suppress("unused")
@Composable
fun ScreenAlert(viewModelItem: ViewModelItem<*, *, *, *>) {
    viewModelItem.screenItemAlertStatus.collectAsStateWithLifecycle().value?.let { itemAlert ->
        AlertDialog(
            onDismissRequest = {
                viewModelItem.clearScreenItemAlert()
                itemAlert.onFinish?.invoke()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModelItem.clearScreenItemAlert()
                    itemAlert.onFinish?.invoke()
                }) {
                    Text(text = "Dismiss")
                }
            },
            title = {
                Text(text = if (itemAlert.itemState.isOk) "Info" else "Error")
            },
            text = {
                Text(text = if (itemAlert.itemState.isOk) "${itemAlert.itemState.msgOk}" else "${itemAlert.itemState.msgError}")
            }
        )
    }
}
