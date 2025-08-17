package com.fonrouge.base

import com.fonrouge.base.types.StringId
import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import org.litote.kmongo.path
import java.util.regex.Pattern
import kotlin.reflect.KProperty

@Suppress("unused")
fun KProperty<StringId<out Any>?>.regex(pattern: String, options: String): Bson {
    return Filters.regex(path(), pattern, options)
}

@Suppress("unused")
infix fun KProperty<StringId<out Any>?>.regex(pattern: String): Bson {
    return Filters.regex(path(), pattern)
}

@Suppress("unused")
infix fun KProperty<StringId<out Any>?>.regex(pattern: Pattern): Bson {
    return Filters.regex(path(), pattern)
}

@Suppress("unused")
infix fun KProperty<StringId<out Any>?>.regex(pattern: Regex): Bson {
    return Filters.regex(path(), pattern.toPattern())
}
