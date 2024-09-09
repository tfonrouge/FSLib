package com.fonrouge.fsLib.model.apiData

abstract class IApiFilter<MID : Any> {
    open var masterItemId: MID? = null
}

fun <FILT : IApiFilter<MID>, MID : Any> FILT.setMasterItemId(masterItemId: MID?): FILT {
    this.masterItemId = masterItemId
    return this
}
