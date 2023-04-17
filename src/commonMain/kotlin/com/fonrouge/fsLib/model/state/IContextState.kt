package com.fonrouge.fsLib.model.state

interface IContextState {
    val contextClass: String?
    val contextId: String?
    val state: String?
}