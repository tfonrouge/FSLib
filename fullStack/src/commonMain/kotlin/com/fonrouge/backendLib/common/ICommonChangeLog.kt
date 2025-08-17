package com.fonrouge.backendLib.common

import com.fonrouge.backendLib.model.ChangeLogFilter
import com.fonrouge.backendLib.model.IChangeLog
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.OId
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class ICommonChangeLog<ChgLog : IChangeLog<U, UID>, U : IUser<UID>, UID : Any>(
    itemKClass: KClass<ChgLog>,
    idSerializer: KSerializer<OId<IChangeLog<U, UID>>>,
) : ICommonContainer<ChgLog, OId<IChangeLog<U, UID>>, ChangeLogFilter>(
    itemKClass = itemKClass,
    idSerializer = idSerializer,
    apiFilterSerializer = ChangeLogFilter.serializer(),
    labelItem = "Entrada en Log de Cambios",
    labelList = "Entradas en Log de Cambios"
)