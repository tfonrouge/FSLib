package com.fonrouge.fsLib.apiServices

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.KSuspendFunction3
import kotlin.reflect.KSuspendFunction4
import kotlin.reflect.KSuspendFunction5
import kotlin.reflect.KSuspendFunction6
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions

@Suppress("UNCHECKED_CAST", "unused")
suspend fun <API : IApiService> callMethod(api: API, methodName: String?) {
    val f1 = api::class.functions.find { it.name == methodName } ?: return
    when (f1.parameters.size) {
        1 -> respond1(api, f1 as KSuspendFunction1<API, Any>)
        2 -> respond2(api, f1 as KSuspendFunction2<API, *, Any>)
        3 -> respond3(api, f1 as KSuspendFunction3<API, *, *, Any>)
        4 -> respond4(api, f1 as KSuspendFunction4<API, *, *, *, Any>)
        5 -> respond5(api, f1 as KSuspendFunction5<API, *, *, *, *, Any>)
        6 -> respond6(api, f1 as KSuspendFunction6<API, *, *, *, *, *, Any>)
        else -> throw Exception("Method not supported: too many parameters...")
    }
}

suspend inline fun <API : IApiService> respond1(
    api: API,
    func: KSuspendFunction1<API, Any>,
) {
    val ret = func.callSuspend(api)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend inline fun <API : IApiService> respond2(
    api: API,
    func: KSuspendFunction2<API, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val pp1 = plist[0]!!
    val p1 = pp1.let { Json.decodeFromString(s1, it) }

    val ret = func.callSuspend(api, p1)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend inline fun <API : IApiService> respond3(
    api: API,
    func: KSuspendFunction3<API, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val pp1 = plist[0]!!
    val pp2 = plist[1]!!
    val p1 = pp1.let { Json.decodeFromString(s1, it) }
    val p2 = pp2.let { Json.decodeFromString(s2, it) }

    val ret = func.callSuspend(api, p1, p2)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend inline fun <API : IApiService> respond4(
    api: API,
    func: KSuspendFunction4<API, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val pp1 = plist[0]!!
    val pp2 = plist[1]!!
    val pp3 = plist[2]!!
    val p1 = pp1.let { Json.decodeFromString(s1, it) }
    val p2 = pp2.let { Json.decodeFromString(s2, it) }
    val p3 = pp3.let { Json.decodeFromString(s3, it) }

    val ret = func.callSuspend(api, p1, p2, p3)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend inline fun <API : IApiService> respond5(
    api: API,
    func: KSuspendFunction5<API, *, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val s4 = Json.serializersModule.serializer(func.parameters[4].type)
    val pp1 = plist[0]!!
    val pp2 = plist[1]!!
    val pp3 = plist[2]!!
    val pp4 = plist[3]!!
    val p1 = pp1.let { Json.decodeFromString(s1, it) }
    val p2 = pp2.let { Json.decodeFromString(s2, it) }
    val p3 = pp3.let { Json.decodeFromString(s3, it) }
    val p4 = pp4.let { Json.decodeFromString(s4, it) }

    val ret = func.callSuspend(api, p1, p2, p3, p4)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend inline fun <API : IApiService> respond6(
    api: API,
    func: KSuspendFunction6<API, *, *, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val s4 = Json.serializersModule.serializer(func.parameters[4].type)
    val s5 = Json.serializersModule.serializer(func.parameters[5].type)
    val pp1 = plist[0]!!
    val pp2 = plist[1]!!
    val pp3 = plist[2]!!
    val pp4 = plist[3]!!
    val pp5 = plist[4]!!
    val p1 = pp1.let { Json.decodeFromString(s1, it) }
    val p2 = pp2.let { Json.decodeFromString(s2, it) }
    val p3 = pp3.let { Json.decodeFromString(s3, it) }
    val p4 = pp4.let { Json.decodeFromString(s4, it) }
    val p5 = pp5.let { Json.decodeFromString(s5, it) }

    val ret = func.callSuspend(api, p1, p2, p3, p4, p5)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}
