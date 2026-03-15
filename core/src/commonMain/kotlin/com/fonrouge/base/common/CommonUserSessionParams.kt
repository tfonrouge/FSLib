package com.fonrouge.base.common

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.model.UserSessionParams
import com.fonrouge.base.types.StringId

/**
 * Common container for [UserSessionParams] entities.
 */
data object CommonUserSessionParams : ICommonContainer<UserSessionParams, StringId<UserSessionParams>, ApiFilter>(
    itemKClass = UserSessionParams::class,
    filterKClass = ApiFilter::class,
    labelItem = "Session Parameters",
)
