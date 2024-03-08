package com.fonrouge.fsLib.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fonrouge.fsLib.viewModel.ViewModelItem

@Suppress("unused")
@Composable
fun ScreenAlert(viewModelItem: ViewModelItem<*, *, *, *>) {
    viewModelItem.alertState.collectAsStateWithLifecycle().value?.let { itemAlert ->
        AlertDialog(
            onDismissRequest = itemAlert.onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = if (itemAlert.canRetry) {
                        itemAlert.onRetry ?: { viewModelItem.clearAlert() }
                    } else {
                        itemAlert.onAccept
                    }
                ) {
                    Text(text = if (itemAlert.canRetry) "Retry" else "Accept")
                }
            },
            dismissButton = if (itemAlert.canRetry) {
                {
                    TextButton(
                        onClick = {
                            viewModelItem.clearAlert()
                            itemAlert.onCancel()
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            } else {
                {}
            },
            icon = if (itemAlert.simpleState.isOk) {
                {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "info")
                }
            } else {
                {
                    Icon(imageVector = Icons.Default.Error, contentDescription = "error")
                }
            },
            title = {
                Text(text = if (itemAlert.simpleState.isOk) "Info" else "Error")
            },
            text = {
                Text(text = if (itemAlert.simpleState.isOk) "${itemAlert.simpleState.msgOk}" else "${itemAlert.simpleState.msgError}")
            },
            iconContentColor = if (itemAlert.simpleState.isOk) Color.Green else Color.Red
        )
    }
}
