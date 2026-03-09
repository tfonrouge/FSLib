package com.fonrouge.base.api

/**
 * Abstract class representing a generic API filter.
 *
 * @param MID The type of the master item identifier.
 */
abstract class IApiFilter<MID : Any> {
    open var masterItemId: MID? = null
}

/**
 * Sets the master item identifier for the filter and returns the filter.
 *
 * @param masterItemId The new master item identifier to set.
 * @return The filter with the updated master item identifier.
 */
@Suppress("unused")
fun <FILT : IApiFilter<MID>, MID : Any> FILT.setMasterItemId(masterItemId: MID?): FILT {
    this.masterItemId = masterItemId
    return this
}
