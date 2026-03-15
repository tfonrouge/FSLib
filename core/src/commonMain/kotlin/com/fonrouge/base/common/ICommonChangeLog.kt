package com.fonrouge.base.common

import com.fonrouge.base.model.ChangeLogFilter
import com.fonrouge.base.model.IChangeLog
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.OId
import kotlin.reflect.KClass

/**
 * Abstract common container for change log entities.
 *
 * @param ChgLog The change log type extending [IChangeLog].
 * @param U The user type extending [IUser].
 * @param UID The user's ID type.
 * @property itemKClass The KClass of the change log entity.
 */
abstract class ICommonChangeLog<ChgLog : IChangeLog<U, UID>, U : IUser<UID>, UID : Any>(
    itemKClass: KClass<ChgLog>,
) : ICommonContainer<ChgLog, OId<IChangeLog<U, UID>>, ChangeLogFilter>(
    itemKClass = itemKClass,
    filterKClass = ChangeLogFilter::class,
    labelItem = "Change Log Entry",
    labelList = "Change Log Entries"
)
