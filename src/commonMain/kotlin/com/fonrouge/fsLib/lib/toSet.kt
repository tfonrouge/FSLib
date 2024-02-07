package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.serializers.IntId
import com.fonrouge.fsLib.serializers.LongId
import com.fonrouge.fsLib.serializers.OId
import com.fonrouge.fsLib.serializers.StringId

@Suppress("unused")
fun <T : BaseDoc<*>> String.toStringIdSet(): Set<StringId<T>> = split(',').map { StringId<T>(it) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toIntIdSet(): Set<IntId<T>> = split(',').map { IntId<T>(it.toInt()) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toLongIdSet(): Set<LongId<T>> = this.split(',').map { LongId<T>(it.toLong()) }.toSet()

@Suppress("unused")
fun <T : BaseDoc<*>> String.toOIdSet(): Set<OId<T>> = this.split(',').map { OId<T>(it) }.toSet()
