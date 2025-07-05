package com.fonrouge.fsLib.layout

import io.kvision.core.*
import io.kvision.html.TAG
import io.kvision.html.Tag
import io.kvision.html.icon
import io.kvision.panel.SimplePanel
import io.kvision.snabbdom.VNode
import io.kvision.state.ObservableValue
import io.kvision.utils.rem

/**
 * A collapsable panel component with an optional legend and animation support.
 *
 * This class represents a collapsable panel (fieldset) with a legend and dynamic content display.
 * The collapsable behavior can be toggled via the legend, and the visibility of child components
 * is managed accordingly. Optionally, animations can be enabled for smoother transitions.
 *
 * @constructor
 * @param legend The optional text for the legend of the fieldset.
 * @param useAnimation A flag indicating whether animations should be used for showing/hiding child components.
 * @param className Additional CSS classes to style the fieldset.
 * @param init An optional initialization block for further configuration of the panel.
 */
class FieldCollapsablePanel(
    legend: String? = null,
    val useAnimation: Boolean = true,
    className: String? = null,
    init: (FieldCollapsablePanel.() -> Unit)? = null,
) : SimplePanel((className?.let { "$it " } ?: "") + "kv_fieldset") {

    val showStateObs = ObservableValue(true)

    /**
     * The legend of the fieldset.
     */
    @Suppress("unused")
    var legend
        get() = legendComponent.content
        set(value) {
            legendComponent.content = value
        }

    /**
     * The legend component.
     */
    private val legendComponent = Tag(TAG.LEGEND, legend) {
        icon("") {
            marginRight = 0.25.rem
            showStateObs.subscribe {
                icon = if (it) {
                    "fas fa-caret-up"
                } else {
                    "fas fa-caret-down"
                }
            }
        }
        onClick {
            it.stopPropagation()
            singleRender {
                children?.forEach { it: Component ->
                    if (useAnimation && it is Widget) {
                        if (showStateObs.value.not()) it.showAnim() else it.hideAnim()
                    } else {
                        it.visible = showStateObs.value.not()
                    }
                }
            }
            showStateObs.value = !showStateObs.value
        }
    }

    init {
        @Suppress("LeakingThis")
        init?.invoke(this)
    }

    override fun render(): VNode {
        val childrenVNodes = childrenVNodes()
        childrenVNodes.asDynamic().unshift(legendComponent.renderVNode())
        return render("fieldset", childrenVNodes)
    }
}

/**
 * DSL builder extension function.
 *
 * It takes the same parameters as the constructor of the built component.
 */
@Suppress("unused")
fun Container.fieldCollapsablePanel(
    legend: String? = null,
    useAnimation: Boolean = true,
    className: String? = null,
    init: (FieldCollapsablePanel.() -> Unit)? = null,
): FieldCollapsablePanel {
    val fieldCollapsablePanel =
        FieldCollapsablePanel(
            legend = legend,
            useAnimation = useAnimation,
            className = className,
            init = init
        )
    this.add(fieldCollapsablePanel)
    return fieldCollapsablePanel
}
