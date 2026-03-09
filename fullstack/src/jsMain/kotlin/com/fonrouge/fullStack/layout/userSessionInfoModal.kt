package com.fonrouge.fullStack.layout

import com.fonrouge.base.date.secsToHumanText
import com.fonrouge.fullStack.lib.format
import com.fonrouge.fullStack.lib.toDateTimeString
import com.fonrouge.base.model.UserSession
import com.fonrouge.base.state.ItemState
import io.kvision.form.text.textInput
import io.kvision.html.div
import io.kvision.modal.Modal
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toJSDate

/**
 * Displays a modal dialog providing information about a user's session, including details like login time,
 * session duration, and expiration time. Continuously updates session details until the modal is closed.
 *
 * @param USS The type representing the user session service.
 * @param UID The type representing the user's unique identifier.
 * @param userName An optional string representing the user's name to display in the modal. Defaults to null.
 * @param userSessionService The service instance responsible for managing and obtaining user session data.
 * @param function A suspending lambda function that retrieves the current user session state, leveraging the provided user session service.
 */
@Suppress("unused")
fun <USS : Any, UID : Any> userSessionInfoModal(
    userName: String? = null,
    userSessionService: USS,
    function: suspend USS.() -> ItemState<UserSession<UID>>
) {
    var loopActive = true
    val userSessionObs = ObservableValue<UserSession<UID>?>(null)
    val modal = Modal(caption = "User Session Info") {
        div().bind(userSessionObs) { userSession ->
            val now = Clock.System.now()
            val sessionTimeSecs = userSession?.let { now.minus(userSession.loginTime).inWholeSeconds.toInt() }
            val sessionExpireInSecs = userSession?.sessionMaxSecs?.let {
                userSession.loginTime.plus(value = it, unit = DateTimeUnit.SECOND)
                    .minus(now).inWholeSeconds.toInt()
            }
            labelControlGrid(
                "User name:" to textInput(value = userName ?: "?") { disabled = true },
                "Login Time:" to textInput(value = userSession?.loginTime?.toJSDate().toDateTimeString) {
                    disabled = true
                },
                "Session time:" to textInput(
                    value = "${sessionTimeSecs.format(0)} secs : ${
                        sessionTimeSecs?.let { secsToHumanText(sessionTimeSecs) }
                    }"
                ) {
                    disabled = true
                },
                "Max Session time:" to textInput(
                    value = "${userSession?.sessionMaxSecs.format(0)} secs : ${
                        userSession?.sessionMaxSecs?.let { secsToHumanText(it) }
                    }"
                ) {
                    disabled = true
                },
                "Session expire in:" to textInput(
                    value = if (userSession?.sessionMaxSecs == 0) "n/a" else "${sessionExpireInSecs.format(0)} secs : ${
                        sessionExpireInSecs?.let { secsToHumanText(it) }
                    }"
                ) {
                    disabled = true
                }
            )
        }
    }
    modal.show()
    flow<UserSession<UID>> {
        while (loopActive) {
            function(userSessionService).item?.let { userSession: UserSession<UID> ->
                emit(userSession)
            }
            delay(1000)
        }
    }.onEach {
        userSessionObs.value = it
    }.launchIn(CoroutineScope(Dispatchers.Default))
    modal.getElement()?.addEventListener("hidden.bs.modal", {
        loopActive = false
        console.warn("hidden.bs.modal")
    })
    console.warn("finished")
}