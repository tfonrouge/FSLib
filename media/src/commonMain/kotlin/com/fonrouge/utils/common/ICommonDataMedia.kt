package com.fonrouge.utils.common

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.StringId
import com.fonrouge.utils.model.DataMediaFilter
import com.fonrouge.utils.model.IDataMedia
import kotlin.reflect.KClass

/**
 * Abstract common container for data media entities.
 *
 * @param DM The data media type extending [IDataMedia].
 * @param U The user type extending [IUser].
 * @param UID The user's ID type.
 * @property itemKClass The KClass of the data media entity.
 */
abstract class ICommonDataMedia<DM : IDataMedia<U, UID>, U : IUser<UID>, UID : Any>(
    itemKClass: KClass<DM>,
) : ICommonContainer<DM, StringId<IDataMedia<U, UID>>, DataMediaFilter>(
    itemKClass = itemKClass,
    filterKClass = DataMediaFilter::class,
    labelItem = "Document Media",
    labelList = "Document Media List"
) {
    companion object {
        const val DATA_MEDIA_PREFIX = "dataMedia"
    }
}
