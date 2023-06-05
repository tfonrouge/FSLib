package com.fonrouge.fsLib.weekPicker

import com.fonrouge.fsLib.enums.Meses
import io.kvision.core.*
import io.kvision.form.number.numericInput
import io.kvision.form.number.spinnerInput
import io.kvision.html.*
import io.kvision.panel.flexPanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.table.TableType
import io.kvision.table.cell
import io.kvision.table.row
import io.kvision.table.table
import io.kvision.utils.px
import io.kvision.utils.rem
import kotlinx.datetime.internal.JSJoda.LocalDate
import kotlinx.datetime.internal.JSJoda.ZoneId

@Suppress("unused")
fun Container.weekPicker(
    localDate: LocalDate = LocalDate.now(ZoneId.SYSTEM),
    monthColors: List<String> = listOf("violet", "LightBlue", "brown")
): Container {
    val obsDate = ObservableValue(localDate.minusDays(localDate.dayOfWeek().ordinal().toInt() + 0))
    val btnObs = ObservableValue<Boolean?>(null)
    return flexPanel(
        direction = FlexDirection.COLUMN,
        justify = JustifyContent.FLEXSTART
    ) {
        flexPanel(
            direction = FlexDirection.ROW,
            alignItems = AlignItems.CENTER,
            justify = JustifyContent.FLEXSTART,
            useWrappers = true
        ) {
            button(text = "", icon = "fas fa-chevron-left", style = ButtonStyle.OUTLINEDARK) {
                fontSize = 0.75.rem
                onClick {
                    obsDate.value = obsDate.value.minusWeeks(1)
                }
            }
            numericInput(value = obsDate.value.isoWeekOfWeekyear(), decimals = 0) {
                width = 3.rem
                obsDate.subscribe {
                    value = it.isoWeekOfWeekyear()
                }
            }
            button(text = "", icon = "fas fa-chevron-right", style = ButtonStyle.OUTLINEDARK) {
                fontSize = 0.75.rem
                onClick {
                    obsDate.value = obsDate.value.plusDays(7)
                }
            }
            label(content = " Año:") {
                marginLeft = 1.rem
                marginRight = 0.5.rem
            }
            spinnerInput(
                value = obsDate.value.year(),
                min = obsDate.value.year().toInt() - 20,
                max = obsDate.value.year().toInt() + 20,
            ) {
                width = 6.rem
                onChange {
                    value?.let {
                        val d = obsDate.value.withYear(it)
                        obsDate.value = d.minusDays(d.dayOfWeek().ordinal().toInt() + 0)
                    }
                }
                obsDate.subscribe {
                    value = it.year()
                }
            }
            button(text = "", icon = "fas fa-chevron-down") {
                marginLeft = 0.5.rem
                onClick {
                    if (btnObs.value == null) btnObs.value = true
                    else btnObs.value = btnObs.value?.not()
                }
            }
        }
        div {
            val obsMonths = ObservableValue<List<String>>(mutableListOf())
            table(
                headerNames = listOf("W#", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"),
                types = setOf(TableType.STRIPED, TableType.HOVER)
            ) {
                bind(obsDate) { weekStart ->
                    var rDate1 = weekStart.minusWeeks(2)
                    var month = rDate1.month()
                    val monthList = mutableListOf<String>()
                    val firstDayList = mutableListOf<Pair<Number, LocalDate>>()
                    month.ordinal().toInt().let {
                        monthList.add(Meses.values()[it].name)
                    }
                    var colorIndex = 0
                    val now = LocalDate.now(ZoneId.SYSTEM)
                    for (i in 1..6) {
                        val weekNumber = rDate1.isoWeekOfWeekyear()
                        firstDayList.add(weekNumber to rDate1)
                        row {
                            val d = rDate1
                            onClick {
                                it.preventDefault()
                                obsDate.value = d
                                btnObs.value = false
                            }
                            cell(content = "$weekNumber") {
                                setStyle("background-color", "gray")
                                setStyle("color", "white")
                                setStyle("text-align", "center")
                                if (weekNumber == obsDate.value.isoWeekOfWeekyear()) {
                                    border = Border(10.px, BorderStyle.INSET, Color("purple"))
                                }
                            }
                            for (n in 0..6) {
                                if (rDate1.month() != month) {
                                    ++colorIndex
                                    month = rDate1.month()
                                    month.ordinal().toInt().let {
                                        monthList.add(Meses.values()[it].name)
                                    }
                                }
                                cell(content = "${rDate1.dayOfMonth()}") {
                                    setStyle("background-color", monthColors[colorIndex])
                                    setStyle("text-align", "right")
                                    if (rDate1 == now) {
                                        border = Border(3.px, BorderStyle.DASHED, Color("red"))
                                    }
                                }
                                rDate1 = rDate1.plusDays(1)
                            }
                        }
                    }
                    obsMonths.value = monthList
                }
            }
            btnObs.subscribe {
                if (it == null) hide() else
                    if (it) showAnim() else hide()
            }
            div().bind(obsMonths) {
                it.forEachIndexed { i, s ->
                    span(s) {
                        marginLeft = 1.rem
                    }
                    span(
                        rich = true,
                        content = """
                            <span style="color:${monthColors[i]}">&#9632;</span>
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
