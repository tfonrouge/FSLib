package com.fonrouge.utils.version

import com.fonrouge.utils.common.ICommonDataMedia
import kotlin.reflect.KClass

fun documentUploadMediaUrl(klassDoc: KClass<*>): String =
    "/${ICommonDataMedia.DATA_MEDIA_PREFIX}/upload/${klassDoc.simpleName}"

fun documentDownloadMediaUrl(klassDoc: KClass<*>): String =
    "/${ICommonDataMedia.DATA_MEDIA_PREFIX}/download/${klassDoc.simpleName}"
