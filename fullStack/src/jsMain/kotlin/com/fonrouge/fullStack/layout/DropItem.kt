package com.fonrouge.fullStack.layout


import com.fonrouge.base.api.IApiFilter
import com.fonrouge.fullStack.config.ConfigView
import com.fonrouge.fullStack.config.ConfigView.Companion.defaultViewMode
import com.fonrouge.fullStack.config.ConfigViewContainer
import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.config.ConfigViewItem.Companion.defaultViewItemMode
import com.fonrouge.fullStack.config.ConfigViewList
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.dropdown.DropDown
import io.kvision.html.Li

/**
 * Represents an item within a dropdown menu.
 *
 * The `DropItem` class extends the `Li` class and provides functionality for interacting
 * with dropdown menus. It configures the list item to optionally toggle the dropdown
 * when clicked, and allows customization of the item's content, appearance, and behavior.
 *
 * @constructor Creates an instance of `DropItem`.
 * @param text The text to be displayed for the dropdown item.
 * @param rich A flag indicating whether the content should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag specifying whether clicking the item should toggle the nearest parent dropdown. Defaults to `true`.
 * @param className An optional additional CSS class to apply to the dropdown item.
 * @param init A lambda function to allow additional configuration and initialization of the `Li` element.
 */
class DropItem(
    text: String,
    rich: Boolean = false,
    toggleDropDown: Boolean = true,
    className: String? = null,
    init: (Li.() -> Unit) = {}
) : Li(
    content = text,
    rich = rich,
    className = listOfNotNull(className?.takeIf { it.isNotBlank() }, "dropdown-item").toSet().joinToString(" "),
    init = init
) {
    init {
        if (toggleDropDown) {
            setEventListener<Li> {
                click = {
                    toggleNearestParentDropDown()
                }
            }
        }
    }

    private fun toggleNearestParentDropDown() {
        generateSequence(parent) { it.parent }
            .filterIsInstance<DropDown>()
            .firstOrNull()?.toggle()
    }
}

/**
 * Adds a dropdown item to the container.
 *
 * This method creates a `DropItem` instance with the specified text, styling, and behavior.
 * The item can optionally toggle the parent dropdown when clicked and allows customization
 * through an initialization block.
 *
 * @param text The text content of the dropdown item.
 * @param rich A flag indicating whether the text should be processed as rich text. Defaults to `false`.
 * @param toggleDropDown A flag specifying whether the item should toggle the nearest parent dropdown when clicked. Defaults to `true`.
 * @param className An optional additional CSS class for the dropdown item.
 * @param init A lambda function for additional configuration of the dropdown item's properties.
 * @return The created `DropItem` instance.
 */
fun Container.dropItem(
    text: String,
    rich: Boolean = false,
    toggleDropDown: Boolean = false,
    className: String? = null,
    init: (Li.() -> Unit) = {}
): DropItem = DropItem(
    text = text,
    rich = rich,
    toggleDropDown = toggleDropDown,
    className = className,
    init = init
).also(::add)

/**
 * Drops an item into the container and sets up its behavior and visual configuration.
 *
 * @param configViewItem An instance of `ConfigViewItem` that provides details like the label, navigation,
 * and API filter related to the item.
 * @param id The identifier of the item being dropped.
 * @param apiFilter An instance of the API filter associated with the item. Defaults to the `apiFilterInstance`
 * provided by the common container of the `configViewItem`.
 * @param vmode The view mode to be used when navigating to the query read of the item. Defaults to `defaultViewItemMode`.
 * @param toggleDropDown Specifies whether a dropdown toggle behavior is enabled. Defaults to `false`.
 * @param className An optional CSS class name to apply to the item. Defaults to `null`.
 * @param init A lambda function to perform additional setup on the `Li` element representing the item.
 */
@Suppress("unused")
fun <ID : Any, FILT : IApiFilter<*>> Container.dropItem(
    configViewItem: ConfigViewItem<*, *, ID, *, FILT, *>,
    id: ID,
    apiFilter: FILT = configViewItem.commonContainer.apiFilterInstance(),
    vmode: ConfigViewContainer.VMode = defaultViewItemMode,
    toggleDropDown: Boolean = false,
    className: String? = null,
    init: (Li.() -> Unit) = {},
) = dropItem(
    text = configViewItem.label,
    toggleDropDown = toggleDropDown,
    className = className,
) {
    onClick {
        configViewItem.navigateToQueryRead(
            id = id,
            apiFilter = apiFilter,
            vmode = vmode
        )
    }
    init(this)
}

/**
 * Adds a dropdown item to the container based on the provided configuration and API filter.
 *
 * This method creates a `DropItem` using properties from the `ConfigViewList` and the specified parameters.
 * When clicked, the dropdown item navigates to the view represented by the `ConfigViewList`, using the given
 * API filter and view mode. Additional customization can be provided through the `init` block.
 *
 * @param configViewList The configuration of the view associated with the dropdown item. Provides properties such as the label
 * and API filter instance.
 * @param apiFilter An instance of the API filter used for navigating to the view. Defaults to a new instance from the
 * `configViewList.commonContainer`.
 * @param vmode The view mode used for navigation, determining how the view opens. Defaults to `defaultViewMode`.
 * @param toggleDropDown A flag specifying whether the dropdown menu should toggle when the item is clicked. Defaults to `false`.
 * @param className An optional CSS class to apply to the dropdown item for styling purposes.
 * @param init A lambda function for additional customization and behavior of the dropdown item's `Li` (list item) element.
 */
@Suppress("unused")
fun <FILT : IApiFilter<*>> Container.dropItem(
    configViewList: ConfigViewList<*, *, *, *, FILT, *, *>,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
    vmode: ConfigViewContainer.VMode = defaultViewMode,
    toggleDropDown: Boolean = false,
    className: String? = null,
    init: (Li.() -> Unit) = {},
) = dropItem(
    text = configViewList.label,
    toggleDropDown = toggleDropDown,
    className = className,
) {
    onClick {
        configViewList.navigateToView(apiFilter = apiFilter, vmode = vmode)
    }
    init(this)
}

/**
 * Drops a view item into the container with the specified configuration and behavior.
 *
 * @param configView The configuration view that defines the context and label for the item.
 * @param apiFilter The API filter instance associated with the configuration view. Defaults to the filter instance from the common container of the configuration view.
 * @param vmode The view mode specifying how the item should be presented. Defaults to the default view item mode.
 * @param toggleDropDown A flag indicating whether the dropdown should be toggled upon interaction. Defaults to false.
 * @param className An optional CSS class name to apply to the item. Defaults to null.
 * @param init A lambda function for additional item initialization. Defaults to an empty lambda.
 */
@Suppress("unused")
fun <FILT : IApiFilter<*>> Container.dropItem(
    configView: ConfigView<*, *, FILT>,
    apiFilter: FILT = configView.commonContainer.apiFilterInstance(),
    vmode: ConfigViewContainer.VMode = defaultViewMode,
    toggleDropDown: Boolean = false,
    className: String? = null,
    init: (Li.() -> Unit) = {},
) = dropItem(
    text = configView.label,
    toggleDropDown = toggleDropDown,
    className = className
) {
    onClick { configView.navigateToView(apiFilter = apiFilter, vmode = vmode) }
    init(this)
}