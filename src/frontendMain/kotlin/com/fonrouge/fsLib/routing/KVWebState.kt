package com.fonrouge.fsLib.routing

import com.fonrouge.fsLib.view.View

open class KVWebState(

    /* state */
    var errorMessage: String? = null,
    var loginErrors: List<String>? = null,
    var userItemErrors: List<String>? = null,
    var view: View? = null,

    /* loading */
    var appLoading: Boolean = false,
) {
    fun copy(block: KVWebState.() -> Unit): KVWebState {
        block(this)
        return this
    }
}
