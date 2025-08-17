package com.fonrouge.fsUtils.common

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.StringId
import com.fonrouge.fsUtils.model.DataMediaFilter
import com.fonrouge.fsUtils.model.IDataMedia
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class ICommonDataMedia<DM : IDataMedia<U, UID>, U : IUser<UID>, UID : Any>(
    itemKClass: KClass<DM>,
    idSerializer: KSerializer<StringId<IDataMedia<U, UID>>>,
) : ICommonContainer<DM, StringId<IDataMedia<U, UID>>, DataMediaFilter>(
    itemKClass = itemKClass,
    idSerializer = idSerializer,
    apiFilterSerializer = DataMediaFilter.serializer(),
    labelItem = "Media de Documento",
    labelList = "Medias de Documento"
) {
    companion object {
        const val DATA_MEDIA_PREFIX = "dataMedia"
    }
}