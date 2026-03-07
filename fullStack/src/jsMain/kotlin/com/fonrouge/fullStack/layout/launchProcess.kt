package com.fonrouge.fullStack.layout

import com.fonrouge.fullStack.view.AppScope
import kotlinx.coroutines.CoroutineScope

/**
 * Lanza un proceso con un diálogo modal y ejecuta un bloque de código suspendido.
 *
 * Muestra un diálogo modal con un título opcional y un mensaje personalizable mientras ejecuta el bloque
 * de código suspendido proporcionado. Una vez que el bloque se completa, el modal se oculta.
 *
 * Delega a [CoroutineScope.withProgress] usando [AppScope] como scope.
 *
 * @param title Un título opcional para el diálogo modal. Por defecto es `null`.
 * @param text Un mensaje para mostrar en el diálogo modal. Por defecto es "one moment please...".
 * @param block Una función lambda suspendida para ejecutar mientras se muestra el modal.
 */
@Suppress("unused")
fun launchProcess(
    title: String? = null,
    text: String = "one moment please...",
    block: suspend CoroutineScope.() -> Unit
) {
    AppScope.withProgress(title = title, text = text, block = block)
}
