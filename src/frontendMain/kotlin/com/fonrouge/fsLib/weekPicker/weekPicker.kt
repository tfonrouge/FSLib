package com.fonrouge.fsLib.weekPicker

import com.fonrouge.fsLib.enums.Meses
import com.fonrouge.fsLib.lib.firstDayOfDateWeek
import io.kvision.core.*
import io.kvision.form.FormInput
import io.kvision.form.GenericFormComponent
import io.kvision.form.InputSize
import io.kvision.form.ValidationStatus
import io.kvision.form.number.spinnerInput
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.span
import io.kvision.panel.FlexPanel
import io.kvision.panel.SimplePanel
import io.kvision.panel.flexPanel
import io.kvision.panel.hPanel
import io.kvision.state.MutableState
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
import kotlin.js.Date

open class WeekInput(
    value: Date? = null,
    val monthColors: List<String> = listOf("violet", "LightBlue", "brown"),
    init: (WeekInput.() -> Unit)? = null,
) : SimplePanel(), GenericFormComponent<Date?>, FormInput, MutableState<Date?> {
    private var weekPickerWidget2: FlexPanel
    protected val observers = mutableListOf<(Date?) -> Unit>()
    val localDateObservable: ObservableValue<LocalDate?> = ObservableValue(value?.firstDayOfDateWeek)
    val disabledObservable = ObservableValue(false)
    override var value: Date?
        get() = localDateObservable.value?.let {
            Date(
                year = it.year().toInt(),
                month = it.month().value().toInt(),
                day = it.dayOfMonth().toInt()
            )
        }
        set(value) {
            localDateObservable.value = value?.firstDayOfDateWeek
        }

    override fun subscribe(observer: (Date?) -> Unit): () -> Unit {
        observers += observer
        observer(value)
        return {
            observers -= observer
        }
    }

    override var disabled: Boolean
        get() = disabledObservable.value
        set(value) {
            disabledObservable.value = value
        }
    override var name: String? by refreshOnUpdate()
    override var size: InputSize? by refreshOnUpdate()
    override var validationStatus: ValidationStatus?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getState(): Date? {
        TODO("Not yet implemented")
    }

    override fun setState(state: Date?) {
        TODO("Not yet implemented")
    }

    fun onChange(block: (Date?) -> Unit) {
        localDateObservable.subscribe { localDate ->
            block(localDate?.let { firstDayOfLocalDateWeek(it) })
        }
    }

    private fun firstDayOfLocalDateWeek(localDate: LocalDate) =
        localDate.minusDays(localDate.dayOfWeek().ordinal()).let {
            Date(
                year = it.year().toInt(),
                month = it.month().ordinal().toInt(),
                day = it.dayOfMonth().toInt()
            )
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun Container.weekPickerWidget() =
        flexPanel(
            direction = FlexDirection.COLUMN,
            justify = JustifyContent.FLEXSTART
        ) {
            val btnObs = ObservableValue<Boolean?>(null)
            flexPanel(
                direction = FlexDirection.ROW,
                alignItems = AlignItems.CENTER,
                justify = JustifyContent.FLEXSTART,
                useWrappers = true
            ) {
                spinnerInput(
                    value = localDateObservable.value?.isoWeekOfWeekyear(),
                    min = 1,
                    max = 53,
                ) {
                    border = Border()
                    placeholder = "W#"
                    width = 4.rem
                    localDateObservable.subscribe {
                        value = it?.isoWeekOfWeekyear()
                    }
                    onChange {
                        value?.let { weekNum ->
                            val d = localDateObservable.value?.let {
                                it.minusWeeks(it.isoWeekOfWeekyear().toInt().minus(1))
                                    .plusWeeks(weekNum.toInt().minus(1))
                            }
                            localDateObservable.value = d?.minusDays(d.dayOfWeek().ordinal().toInt())
                        } ?: run { localDateObservable.value = null }
                    }
                }
                spinnerInput(
                    value = localDateObservable.value?.year(),
                    min = localDateObservable.value?.year()?.toInt()?.minus(20),
                    max = localDateObservable.value?.year()?.toInt()?.plus(20),
                ) {
                    placeholder = "Year"
                    width = 5.rem
                    onChange {
                        value?.let {
                            val d = localDateObservable.value?.withYear(it)
                            localDateObservable.value = d?.minusDays(d.dayOfWeek().ordinal().toInt())
                        } ?: run { localDateObservable.value = null }
                    }
                    localDateObservable.subscribe {
                        value = it?.year()
                    }
                }
                button(text = "", icon = "fas fa-chevron-down", style = ButtonStyle.OUTLINEDARK) {
                    fontSize = 0.75.rem
                    marginLeft = 0.5.rem
                    onClick {
                        if (btnObs.value == null) btnObs.value = true
                        else btnObs.value = btnObs.value?.not()
                    }
                    btnObs.subscribe {
                        icon = if (it == true) "fas fa-chevron-up" else "fas fa-chevron-down"
                    }
                }
            }
            div {
                val obsMonths = ObservableValue<List<String>>(mutableListOf())
                table(
                    headerNames = listOf("W#", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"),
                    types = setOf(TableType.STRIPED, TableType.HOVER)
                ) {
                    bind(localDateObservable) { weekStart0 ->
                        val weekStart = weekStart0 ?: LocalDate.now(ZoneId.SYSTEM)
                        var rDate1 = weekStart.minusWeeks(2)
                        var month = rDate1.month()
                        val monthList = mutableListOf<String>()
                        val firstDayList = mutableListOf<Pair<Number?, LocalDate?>>()
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
                                    localDateObservable.value = d
                                    btnObs.value = false
                                }
                                if (weekNumber == localDateObservable.value?.isoWeekOfWeekyear()) {
                                    border = Border(10.px, BorderStyle.INSET, Color("purple"))
                                }
                                cell(content = "$weekNumber") {
                                    setStyle("background-color", "gray")
                                    setStyle("color", "white")
                                    setStyle("text-align", "center")
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
                                        setStyle("text-align", "center")
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
                    if (it == null) hideAnim() else
                        if (it) showAnim() else hideAnim()
                }
                div().bind(obsMonths) {
                    hPanel(alignItems = AlignItems.CENTER) {
                        it.forEachIndexed { i, s ->
                            span("$s:") {
                                marginLeft = 1.rem
                            }
                            span(
                                rich = true,
                                content = "&#9632;",
                            ) {
                                setStyle("color", monthColors[i])
                                fontSize = 2.rem
                            }
                        }
                    }
                }
            }
        }

    init {
        weekPickerWidget2 = weekPickerWidget()
        init?.invoke(this)
    }
}

@Suppress("unused")
fun Container.weekPickerInput(
    value: Date? = null,
    init: (WeekInput.() -> Unit)? = null,
): WeekInput {
    val weekInput = WeekInput(
        value = value,
        init = init,
    )
    add(weekInput)
    return weekInput
}
