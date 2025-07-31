package com.fonrouge.modelUtils.model

import com.fonrouge.fsLib.model.base.IUser
import com.fonrouge.fsLib.types.StringId
import com.fonrouge.modelUtils.common.ICommonDataMedia
import io.kvision.types.OffsetDateTime

interface IDataMedia<U : IUser<out UID>, UID : Any> : IWorkLogBase<StringId<IDataMedia<U, UID>>> {
    override val _id: StringId<IDataMedia<U, UID>>
    val classifierDoc: String
    val serializedIdDoc: String
    val fechaCreacion: OffsetDateTime
    val fileName: String
    val fileSize: Long
    val contentType: String
    val contentSubtype: String
    val order: String?
    val userId: UID
    val hasThumbnail: Boolean
    val thumbnailBuildError: String?

    val user: U?
    val dataMediaPath: String get() = "${ICommonDataMedia.DATA_MEDIA_PREFIX}/$classifierDoc/$_id"
    val filePath: String get() = "$dataMediaPath.$contentSubtype"
    val thumbnailPath: String get() = "$dataMediaPath-thumbnail.jpeg"
}
