package com.fonrouge.modelUtils.version

import com.fonrouge.modelUtils.common.ICommonDataMedia
import kotlin.reflect.KClass

fun documentUploadMediaUrl(klassDoc: KClass<*>): String =
    "/${ICommonDataMedia.DATA_MEDIA_PREFIX}/upload/${klassDoc.simpleName}"

fun documentDownloadMediaUrl(klassDoc: KClass<*>): String =
    "/${ICommonDataMedia.DATA_MEDIA_PREFIX}/download/${klassDoc.simpleName}"
