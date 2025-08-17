package com.fonrouge.base.api

import kotlinx.serialization.Serializable

/**
 * Data class representing an API filter.
 *
 * This class is a concrete implementation of the [IApiFilter] abstract class
 * which provides a structure for filtering API requests based on a `masterItemId`.
 */
@Suppress("unused")
@Serializable
data class ApiFilter(
    override var masterItemId: Unit? = null,
) : IApiFilter<Unit>()
