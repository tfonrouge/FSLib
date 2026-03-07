package com.fonrouge.fullStack.help

/**
 * Identifies a help module grouping for views.
 *
 * Implement this interface (typically as a `sealed class`) in consumer applications
 * to define the available modules. Each module maps to a subdirectory under `help-docs/`
 * where the module manual and per-view help files are stored.
 *
 * Example:
 * ```kotlin
 * sealed class AppModule(
 *     override val slug: String,
 *     override val displayName: String
 * ) : IHelpModule {
 *     data object Importaciones : AppModule("importaciones", "Gestión de Importaciones")
 *     data object Inventario : AppModule("inventario", "Inventario")
 * }
 * ```
 *
 * @property slug Directory name under `help-docs/` (e.g. `"importaciones"`).
 * @property displayName Human-readable module name shown in the UI.
 */
interface IHelpModule {
    val slug: String
    val displayName: String
}
