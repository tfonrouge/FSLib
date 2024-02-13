package com.fonrouge.fsLib.lib

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
    init: (Button.() -> Unit)? = null
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
                    this.icon = "fas fa-caret-up"
                } else {
                    this.icon = "fas fa-caret-down"
                }
            }
        }
        div().bind(menuOpen) {
//            marginTop = 10.px
            if (it) show() else hide()
            table(
                types = setOf(TableType.BORDERED, TableType.SMALL, TableType.HOVER)
            ) {
                options.forEach { pair ->
                    row {
                        cell(pair.first) {
                            paddingLeft = 1.rem
                            paddingRight = 1.rem
                            cursor = Cursor.POINTER
                            onClick {
                                menuOpen.value = false
                                window.open(url = pair.second, target = "_blank")
                            }
                        }
                    }
                }
            }
        }
    }
}
