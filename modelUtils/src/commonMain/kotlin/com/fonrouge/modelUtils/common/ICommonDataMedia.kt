package com.fonrouge.modelUtils.common

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.IUser
import com.fonrouge.fsLib.types.StringId
import com.fonrouge.modelUtils.model.DataMediaFilter
import com.fonrouge.modelUtils.model.IDataMedia
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
