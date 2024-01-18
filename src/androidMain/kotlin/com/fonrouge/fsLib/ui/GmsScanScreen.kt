package com.fonrouge.fsLib.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fonrouge.fsLib.viewModel.ViewModelCamera
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalGetImage::class)
@Composable
fun GmsScanScreen(
    viewModelCamera: ViewModelCamera = viewModel(),
    context: Context = LocalContext.current,
    onFailure: (Exception) -> Unit = {},
    onCanceled: () -> Unit = {},
    onReadBarcode: (CodeEntry) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    if (viewModelCamera.uiState.collectAsState().value.scannerOpen) {
        val scanner = GmsBarcodeScanning
            .getClient(context, viewModelCamera.gmsBarcodeScannerOptions)
        scanner.startScan()
            .addOnSuccessListener { barcode: Barcode ->
                onReadBarcode(
                    CodeEntry(
                        source = CodeEntry.Type.Camera,
                        barcode = barcode,
                        code = barcode.rawValue
                    )
                )
                viewModelCamera.onEvent(ViewModelCamera.UIEvent.CodeRead(barcode.rawValue))
            }
            .addOnCanceledListener {
                onCanceled()
            }
            .addOnFailureListener { exception: Exception ->
                onFailure(exception)
            }
            .addOnCompleteListener {
                viewModelCamera.onEvent(ViewModelCamera.UIEvent.Close)
            }
    } else {
        content()
    }
}