package com.fonrouge.fsLib.model.state

interface ISimpleState {
    val isOk: Boolean
    val msgOk: String?
    val msgError: String?
    val state: String?
}
