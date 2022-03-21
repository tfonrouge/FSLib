package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.model.base.BaseContainerItem
import com.fonrouge.fsLib.model.base.BaseContainerList
import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.types.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
class MediaItem(
    override var id: String,
    @Contextual
    var uploaded: LocalDateTime,
    @Contextual
    var lastRequested: LocalDateTime?,
    var className: String,
    var itemId: String,
    var context: String,
    var contentType: String,
    var pathFileName: String,
    var fileName: String,
    var size: Long,
    var checksum: Long,
    var user: String,
    var url: String? = null,
) : BaseModel<String>() {
//    override var upsertInfo: UpsertInfo? = null
}

@Serializable
//@JsExport
class MediaItemContainerItem(
    override var item: MediaItem?,
) : BaseContainerItem<MediaItem>() {
    override var version: String? = null

    @Contextual
    override var date: LocalDateTime? = null
}

@Serializable
//@JsExport
class MediaItemContainerList(
    override var list: Array<MediaItem>,
) : BaseContainerList<MediaItem>() {
    override var listCRC32: String? = null
    override var version: String? = null

    @Contextual
    override var date: LocalDateTime? = null
}
