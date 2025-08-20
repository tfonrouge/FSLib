package com.fonrouge.base.common

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.model.UserSessionParams
import com.fonrouge.base.types.StringId

data object CommonUserSessionParams : ICommonContainer<UserSessionParams, StringId<UserSessionParams>, ApiFilter>(
    itemKClass = UserSessionParams::class,
    idSerializer = StringId.serializer(UserSessionParams.serializer()),
    apiFilterSerializer = ApiFilter.serializer(),
    labelItem = "Parámetros de la Sesión",
)
