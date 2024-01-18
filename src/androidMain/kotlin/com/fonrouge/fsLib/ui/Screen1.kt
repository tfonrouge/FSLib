package com.fonrouge.fsLib.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Suppress("unused")
@Composable
fun Screen1(
    vm: MyVM = viewModel()
) {
    Box {
        Text(text = "Hello from Juana La Cubana")
    }
}

class MyVM : ViewModel()