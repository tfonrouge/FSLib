package com.fonrouge.fullStack.lib

import io.kvision.core.*
import io.kvision.html.*
import io.kvision.panel.vPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.table.TableType
import io.kvision.table.cell
import io.kvision.table.row
import io.kvision.table.table
import io.kvision.utils.rem
import kotlinx.browser.window

/**
 * Creates a button menu component that toggles a dropdown menu and can be configured
 * with various styles, options, and behaviors.
 *
 * @param text The text displayed on the button.
 * @param icon The optional icon displayed on the button.
 * @param openStyle The button style applied when the menu is open.
 * @param closedStyle The button style applied when the menu is closed.
 * @param type The type of the button (default is `ButtonType.BUTTON`).
 * @param disabled Specifies whether the button is disabled.
 * @param separator Separator text or character to use between button components.
 * @param labelFirst Determines if the label should appear before or after the icon.
 * @param className Additional custom CSS class name for the button.
 * @param menuOpen An observable value that determines the current open/close state of the menu.
 * @param options A list of pairs, where each pair represents a dropdown menu item with a label (first) and a url (second).
 * @param init An optional initialization block for additional button configuration.
 */
@Suppress("unused")
fun Container.buttonMenu(
    text: String,
    icon: String? = null,
    openStyle: ButtonStyle = ButtonStyle.OUTLINEPRIMARY,
    closedStyle: ButtonStyle = ButtonStyle.PRIMARY,
    type: ButtonType = ButtonType.BUTTON,
    disabled: Boolean = false,
    separator: String? = null,
    labelFirst: Boolean = true,
    className: String? = null,
    menuOpen: ObservableValue<Boolean> = ObservableValue(false),
    options: List<Pair<String, String>> = listOf(),
    init: (Button.() -> Unit)? = null,
) {
    vPanel(alignItems = AlignItems.CENTER) {
        button(
            text = text,
            icon = icon,
            style = openStyle,
            type = type,
            disabled = disabled,
            separator = separator,
            labelFirst = labelFirst,
            className = className,
            init = init
        ).apply {
            onClick {
                menuOpen.value = menuOpen.value.not()
            }
            bind(menuOpen) {
                if (it) {
                    this.style = closedStyle
                } else {
                    this.style = openStyle
                }
            }
        }
        icon(icon = "") {
            color = Color("lightgray")
            cursor = Cursor.POINTER
            onClick { menuOpen.value = menuOpen.value.not() }
            bind(menuOpen) {
                if (it) {
                    this.icon = "fas fa-caret-down"
                } else {
                    this.icon = "fas fa-caret-up"
                }
            }
        }
        div().bind(menuOpen) {
            table(
                types = setOf(TableType.BORDERED, TableType.SMALL, TableType.HOVER),
                className = "buttonMenuList"
            ) {
                if (it) show() else hide()
                options.forEach { pair ->
                    row {
                        cell(pair.first) {
                            paddingLeft = 1.rem
                            paddingRight = 1.rem
                            cursor = Cursor.POINTER
                            onClick {
                                window.open(url = pair.second, target = "_blank")
                            }
                        }
                    }
                }
            }
        }
    }
}
