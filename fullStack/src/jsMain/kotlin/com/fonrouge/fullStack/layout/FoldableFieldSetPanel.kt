package com.fonrouge.fullStack.layout

import io.kvision.core.*
import io.kvision.html.TAG
import io.kvision.html.Tag
import io.kvision.html.icon
import io.kvision.panel.SimplePanel
import io.kvision.snabbdom.VNode
import io.kvision.state.ObservableValue
import io.kvision.utils.rem

/**
 * A UI component representing a foldable fieldset panel.
 *
 * This class creates a fieldset with a collapsible/expandable mechanism,
 * which includes an optional legend and uses animations for the toggle effect.
 * It extends the `SimplePanel` class and allows further customization
 * through the provided initialization lambda.
 *
 * @param legend The optional legend text for the fieldset.
 * @param useAnimation A flag indicating whether to use animations during toggle. Defaults to true.
 * @param className Additional CSS classes for the fieldset panel.
 * @param init An optional initialization block to configure the instance.
 */
class FoldableFieldSetPanel(
    legend: String? = null,
    val useAnimation: Boolean = true,
    className: String? = null,
    init: (FoldableFieldSetPanel.() -> Unit)? = null,
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
                    this@Tag.title = "Collapse"
                    "fas fa-caret-up"
                } else {
                    this@Tag.title = "Expand"
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
 * Adds a foldable fieldset panel to the container.
 *
 * This method creates and appends a `FoldableFieldSetPanel` instance to the current container.
 * The fieldset panel includes an optional legend, an animation toggle for collapsibility, and allows
 * further customization through an initialization block.
 *
 * @param legend The optional legend text to display in the fieldset.
 * @param useAnimation A flag indicating whether to use animation when toggling the fieldset's visibility. Defaults to true.
 * @param className Additional CSS classes to apply to the fieldset.
 * @param init An optional lambda function to further configure the `FoldableFieldSetPanel` instance.
 * @return The created `FoldableFieldSetPanel` instance that has been added to the container.
 */
@Suppress("unused")
fun Container.foldableFieldSetPanel(
    legend: String? = null,
    useAnimation: Boolean = true,
    className: String? = null,
    init: (FoldableFieldSetPanel.() -> Unit)? = null,
): FoldableFieldSetPanel {
    val foldableFieldSetPanel =
        FoldableFieldSetPanel(
            legend = legend,
            useAnimation = useAnimation,
            className = className,
            init = init
        )
    this.add(foldableFieldSetPanel)
    return foldableFieldSetPanel
}
