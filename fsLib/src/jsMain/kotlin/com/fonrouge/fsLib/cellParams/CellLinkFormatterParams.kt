package com.fonrouge.fsLib.cellParams

import io.kvision.tabulator.js.Tabulator
import js.objects.jso

/**
 * Represents the configuration parameters for the "link" formatter in a Tabulator table cell.
 * This interface allows customization of the behavior, appearance, and functionality of links
 * displayed within a cell.
 *
 * @property labelField Specifies the field in the row data to use as the label for the link. If set, this overrides other label options.
 * @property label Defines a static label for the link. Can be null if dynamic label is preferred.
 * @property labelAsString When provided, defines the label as a string for the link.
 * @property labelAsFun A function that generates a label string dynamically based on the cell component.
 * @property urlPrefix An optional prefix to prepend to the link's URL.
 * @property urlField Specifies the field in the row data to use for generating the URL. This overrides other URL options if set.
 * @property url Defines a static URL for the link. Can be null if dynamic URL is preferred.
 * @property urlAsString When provided, defines the URL as a string for the link.
 * @property urlAsFun A function that generates a URL string dynamically based on the cell component.
 * @property target Specifies the target attribute of the link (e.g., "_blank" for opening in a new tab).
 * @property download Configures the download attribute of the link. Can be set to static values or derived dynamically.
 * @property downloadAsBoolean Represents the download attribute as a boolean value when applicable.
 * @property downloadAsString Represents the download attribute as a string value.
 * @property downloadAsFun A function that generates the download attribute value dynamically based on the cell component.
 */
external interface CellLinkFormatterParams {
    var labelField: String?
    var label: Any?
    var labelAsString: String?
    var labelAsFun: ((cell: Tabulator.CellComponent) -> String?)?
    var urlPrefix: String?
    var urlField: String?
    var url: Any?
    var urlAsString: String?
    var urlAsFun: ((cell: Tabulator.CellComponent) -> String?)?
    var target: String?
    var download: Any?
    var downloadAsBoolean: Boolean?
    var downloadAsString: String?
    var downloadAsFun: ((cell: Tabulator.CellComponent) -> String?)?
}

/**
 * Configures and returns a `CellLinkFormatterParams` object representing parameters for a "link" formatter
 * in a Tabulator table cell. This method applies the user-defined configuration block to initialize
 * and customize the formatter parameters.
 *
 * @param block A lambda function used to configure the `CellLinkFormatterParams` instance.
 * @return A configured instance of `CellLinkFormatterParams` with the applied customizations.
 */
fun cellLinkFormatterParams(block: CellLinkFormatterParams.() -> Unit): CellLinkFormatterParams {
    val result = jso(block)
    result.labelAsString?.let { result.label = it }
    result.labelAsFun?.let { result.label = it }
    result.urlAsString?.let { result.url = it }
    result.urlAsFun?.let { result.url = it }
    result.downloadAsBoolean?.let { result.download = it }
    result.downloadAsString?.let { result.download = it }
    result.downloadAsFun?.let { result.download = it }
    return result
}