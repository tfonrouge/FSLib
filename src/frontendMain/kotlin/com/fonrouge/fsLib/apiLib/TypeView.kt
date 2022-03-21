package com.fonrouge.fsLib.apiLib

enum class TypeView(val label: String) {
    None(""),
    Item("/item"),
    List("/list"),
    CItem("/citem"),
    CList("/clist"),
    SelectList("/selectList"),
    Upsert("/upsert")
}
