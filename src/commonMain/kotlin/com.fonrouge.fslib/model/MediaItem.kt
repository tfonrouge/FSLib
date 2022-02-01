package com.fonrouge.fslib.model

import com.fonrouge.fslib.model.base.BaseContainerItem
import com.fonrouge.fslib.model.base.BaseContainerList
import com.fonrouge.fslib.model.base.BaseModel
import kotlinx.datetime.LocalDateTime
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
//    override var upsertInfo: UpsertInfo = UpsertInfo("","", "", "", "")
}

@Serializable
class MediaItemContainerItem(
    override var item: MediaItem?,
) : BaseContainerItem<MediaItem>()

@Serializable
class MediaItemContainerList(
    override var list: Array<MediaItem>,
) : BaseContainerList<MediaItem>()
