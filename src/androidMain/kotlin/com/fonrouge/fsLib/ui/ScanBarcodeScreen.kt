package com.fonrouge.fsLib.ui

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import com.fonrouge.fsLib.viewModel.ViewModelCamera
import com.google.mlkit.vision.barcode.common.Barcode

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScanBarcodeScreen(
    viewModelCamera: ViewModelCamera,
    onReadBarcode: (CodeEntry) -> Unit = {},
    onFilter: ((Barcode) -> Boolean)? = null,
    content: @Composable () -> Unit,
) {
    when (viewModelCamera.selectedCameraType.value) {
        ViewModelCamera.CameraType.GooglePlay -> {
            GmsScanScreen(
                viewModelCamera = viewModelCamera,
                onReadBarcode = onReadBarcode,
                content = content
            )
        }

        ViewModelCamera.CameraType.CameraX -> {
            CameraXCoreReaderScreen1(
                viewModelCamera = viewModelCamera,
                onReadBarcode = onReadBarcode,
                onFilter = onFilter,
                content = content
            )
        }
    }
}

data class CodeEntry(
    val source: Type,
    val barcode: Barcode? = null,
    val code: String? = null,
) {
    enum class Type {
        Camera,
        Keyboard,
    }
}