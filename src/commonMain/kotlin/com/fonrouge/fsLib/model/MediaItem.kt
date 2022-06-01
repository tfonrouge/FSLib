package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.model.base.BaseModel
import io.kvision.types.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
class MediaItem(
    override var _id: String,
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
) : BaseModel<String> {
//    override var upsertInfo: UpsertInfo? = null
}
