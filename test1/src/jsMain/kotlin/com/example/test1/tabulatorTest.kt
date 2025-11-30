package com.example.test1

import com.fonrouge.fullStack.cellValidator.cellValidatorAsFun
import io.kvision.core.Container
import io.kvision.tabulator.*
import kotlinx.serialization.Serializable

fun Container.tabulatorTest() {
    @Serializable
    data class Person(val name: String, val age: Int)
    tabulator(
        data = listOf(
            Person("John", 21),
            Person("Mary", 24),
            Person("Susan", 18),
        ),
        options = TabulatorOptions(
            columns = listOf(
                ColumnDefinition(
                    title = "Name",
                    field = "name",
                ),
                ColumnDefinition(
                    title = "Age",
                    field = "age",
                    editor = Editor.NUMBER,
                    validator = Validator.NUMERIC,
//                    validatorFunction = { cell: Tabulator.CellComponent, value: dynamic, c: dynamic ->
//                        console.warn("validatorFunction called", cell, value, c)
//                        console.warn("data:", cell.getData(), "value: '$value'", "typeOf:", jsTypeOf(value))
//                        false
//                    }
                    validatorFunction = cellValidatorAsFun { cell, value, parameters ->
                        console.warn("validatorFunction called", cell, value, parameters)
                        console.warn("data:", cell.getData(), "value: '$value'", "typeOf:", jsTypeOf(value))
                        jsTypeOf(value) == "number" && (value as Number).toInt() in listOf(
                            18,
                            21,
                            24,
                        )
                    }
                ),
            )
        )
    )
}
