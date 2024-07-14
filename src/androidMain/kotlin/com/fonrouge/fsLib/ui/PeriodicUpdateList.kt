package com.fonrouge.fsLib.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.fonrouge.fsLib.viewModel.ViewList
import kotlinx.coroutines.delay

@Suppress("unused")
@Composable
fun PeriodicUpdateList(
    viewModel: ViewList<*, *, *, *>,
    periodicUpdate: Boolean? = null,
    periodicInterval: Int? = null,
) {
    periodicUpdate?.let { viewModel.periodicUpdate = it }
    periodicInterval?.let { viewModel.periodicInterval = it }
    LaunchedEffect(key1 = viewModel.refreshListCounter) {
        delay(viewModel.periodicInterval.toLong())
        if (viewModel.periodicUpdate) {
            ++viewModel.refreshListCounter
        }
//        Log.d("LaunchedEffect", "${viewModel.refreshListCounter}")
        viewModel.requestRefresh = true
    }
}