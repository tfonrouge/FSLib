package com.fonrouge.fsLib.commonServices

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions

/**
 * Calls a specified method on the given API service and handles the response.
 *
 * @param API The type of the API service that implements `IApiCommonService`.
 * @param api The instance of the API service where the method should be called.
 * @param methodName The name of the method to be called on the API service.
 */
@Suppress("UNCHECKED_CAST", "unused")
suspend fun <API : IApiCommonService> callMethod(api: API, methodName: String?) {
    if (methodName.isNullOrBlank()) {
        api.call.respondText(
            text = "Missing method name",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val f1 = api::class.functions.find { it.name == methodName }
    if (f1 == null) {
        api.call.respondText(
            text = "Method '$methodName' not found",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.NotFound
        )
        return
    }
    when (f1.parameters.size) {
        1 -> respond1(api, f1 as KSuspendFunction1<API, Any>)
        2 -> respond2(api, f1 as KSuspendFunction2<API, *, Any>)
        3 -> respond3(api, f1 as KSuspendFunction3<API, *, *, Any>)
        4 -> respond4(api, f1 as KSuspendFunction4<API, *, *, *, Any>)
        5 -> respond5(api, f1 as KSuspendFunction5<API, *, *, *, *, Any>)
        6 -> respond6(api, f1 as KSuspendFunction6<API, *, *, *, *, *, Any>)
        else -> api.call.respondText(
            text = "Method not supported: too many parameters",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
    }
}

suspend fun <API : IApiCommonService> respond1(
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

suspend fun <API : IApiCommonService> respond2(
    api: API,
    func: KSuspendFunction2<API, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    if (plist.size < 1 || plist[0] == null) {
        api.call.respondText(
            text = "Missing required parameter #1",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val pp1 = plist[0]
    val p1 = try { Json.decodeFromString(s1, pp1!!) } catch (e: SerializationException) {
        api.call.respondText(
            text = "Invalid parameter #1: ${e.message}",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }

    val ret = func.callSuspend(api, p1)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend fun <API : IApiCommonService> respond3(
    api: API,
    func: KSuspendFunction3<API, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    if (plist.size < 2 || plist[0] == null || plist[1] == null) {
        api.call.respondText(
            text = "Missing required parameters",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val pp1 = plist[0]
    val pp2 = plist[1]
    val p1 = try { Json.decodeFromString(s1, pp1!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #1: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p2 = try { Json.decodeFromString(s2, pp2!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #2: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }

    val ret = func.callSuspend(api, p1, p2)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend fun <API : IApiCommonService> respond4(
    api: API,
    func: KSuspendFunction4<API, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    if (plist.size < 3 || plist[0] == null || plist[1] == null || plist[2] == null) {
        api.call.respondText(
            text = "Missing required parameters",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val pp1 = plist[0]
    val pp2 = plist[1]
    val pp3 = plist[2]
    val p1 = try { Json.decodeFromString(s1, pp1!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #1: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p2 = try { Json.decodeFromString(s2, pp2!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #2: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p3 = try { Json.decodeFromString(s3, pp3!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #3: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }

    val ret = func.callSuspend(api, p1, p2, p3)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend fun <API : IApiCommonService> respond5(
    api: API,
    func: KSuspendFunction5<API, *, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    if (plist.size < 4 || plist[0] == null || plist[1] == null || plist[2] == null || plist[3] == null) {
        api.call.respondText(
            text = "Missing required parameters",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val s4 = Json.serializersModule.serializer(func.parameters[4].type)
    val pp1 = plist[0]
    val pp2 = plist[1]
    val pp3 = plist[2]
    val pp4 = plist[3]
    val p1 = try { Json.decodeFromString(s1, pp1!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #1: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p2 = try { Json.decodeFromString(s2, pp2!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #2: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p3 = try { Json.decodeFromString(s3, pp3!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #3: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p4 = try { Json.decodeFromString(s4, pp4!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #4: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }

    val ret = func.callSuspend(api, p1, p2, p3, p4)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}

suspend fun <API : IApiCommonService> respond6(
    api: API,
    func: KSuspendFunction6<API, *, *, *, *, *, Any>,
) {
    val plist = api.call.receive<List<String?>>()
    if (
        plist.size < 5 ||
        plist[0] == null ||
        plist[1] == null ||
        plist[2] == null ||
        plist[3] == null ||
        plist[4] == null
    ) {
        api.call.respondText(
            text = "Missing required parameters",
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.BadRequest
        )
        return
    }
    val s1 = Json.serializersModule.serializer(func.parameters[1].type)
    val s2 = Json.serializersModule.serializer(func.parameters[2].type)
    val s3 = Json.serializersModule.serializer(func.parameters[3].type)
    val s4 = Json.serializersModule.serializer(func.parameters[4].type)
    val s5 = Json.serializersModule.serializer(func.parameters[5].type)
    val pp1 = plist[0]
    val pp2 = plist[1]
    val pp3 = plist[2]
    val pp4 = plist[3]
    val pp5 = plist[4]
    val p1 = try { Json.decodeFromString(s1, pp1!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #1: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p2 = try { Json.decodeFromString(s2, pp2!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #2: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p3 = try { Json.decodeFromString(s3, pp3!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #3: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p4 = try { Json.decodeFromString(s4, pp4!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #4: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }
    val p5 = try { Json.decodeFromString(s5, pp5!!) } catch (e: SerializationException) {
        api.call.respondText("Invalid parameter #5: ${e.message}", ContentType.Text.Plain, HttpStatusCode.BadRequest); return
    }

    val ret = func.callSuspend(api, p1, p2, p3, p4, p5)

    val serRet = Json.serializersModule.serializer(func.returnType)
    val ser = Json.encodeToString(serRet, ret)
    api.call.respondText(
        text = ser,
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
}
