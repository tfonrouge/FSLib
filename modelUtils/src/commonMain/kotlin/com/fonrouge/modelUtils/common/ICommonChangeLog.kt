package com.fonrouge.modelUtils.common

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.types.OId
import com.fonrouge.modelUtils.model.ChangeLogFilter
import com.fonrouge.modelUtils.model.IChangeLog
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
