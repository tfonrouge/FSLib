package com.fonrouge.fsLib.layout

import io.kvision.core.*
import io.kvision.html.span
import io.kvision.panel.GridPanel
import io.kvision.panel.gridPanel
import io.kvision.utils.rem

/**
 * Creates a grid layout with labeled controls, where each label is aligned with its associated component.
 *
 * This method generates a grid with two columns: the first column contains labels,
 * and the second column contains the corresponding components. The grid layout is configured
 * with automatic sizing for the label column and flexible sizing for the component column.
 * The labels are styled to align with the associated components.
 *
 * @param entry A vararg of pairs where each pair consists of a `String` label
 * and a `Component` to be displayed in the grid. The label will be shown
 * in the first column, and the component will be displayed in the second column.
 */
@Suppress("unused")
fun Container.labelControlGrid(vararg entry: Pair<String, Component>, init: (GridPanel.() -> Unit)? = null) {
    gridPanel(templateColumns = "auto 1fr", columnGap = 5, rowGap = 5) {
        init?.invoke(this)
        entry.forEach {
            span("${it.first}:") {
                display = Display.FLEX
                fontSize = 0.75.rem
                justifyContent = JustifyContent.FLEXEND
                alignItems = AlignItems.CENTER
            }
            add(it.second)
        }
    }
}
