package com.fonrouge.fullStack.mongoDb

import org.litote.kmongo.json

/**
 * Extension property for any object that processes the `json` property of the object
 * and replaces instances of JSON date notation `{"$date":"value"}` with the `ISODate("value")` format.
 *
 * This property uses a regular expression to identify and transform the target substring.
 * It is useful for reformatting JSON data containing MongoDB-style date fields.
 */
val Any.json2: String
    get() = this.json
        .replace(
            regex = "\\{\\s*\"\\\$date\"\\s*:\\s*\"([^\"]+)\"\\s*}".toRegex(),
            replacement = "ISODate(\"$1\")"
        )
        .replace(
            regex = "\\{\\s*\"\\\$oid\"\\s*:\\s*\"([^\"]+)\"\\s*}".toRegex(),
            replacement = "ObjectId(\"$1\")"
        )
        .replace(
            regex = "\\{\\s*\"\\\$regularExpression\"\\s*:\\s*\\{\\s*\"pattern\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"options\"\\s*:\\s*\"([^\"]*)\"\\s*}\\s*}".toRegex(),
            replacement = "RegExp(\"$1\", \"$2\")"
        )