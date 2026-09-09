package com.fonrouge.fullStack.form

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the [InFlightValueGuard] state machine backing `FsTomSelectRemoteInput`'s duplicate-load
 * suppression (blueprints/view-consumer-gaps CONTRACT I-1/I-2, OC-01).
 *
 * The guard has no separate failure API on purpose: completion and failure both call
 * [InFlightValueGuard.settle], so the retry-after-failure invariant (I-2) is the same assertion
 * as clear-after-completion. The guard reports state ([InFlightValueGuard.isInFlight]); the
 * actual drop of a duplicate call lives in `FsTomSelectRemoteInput.refreshState`, which consults
 * it before delegating to the base class.
 */
class InFlightValueGuardTest {

    @Test
    fun nothingIsInFlightInitially() {
        val guard = InFlightValueGuard()
        assertFalse(guard.isInFlight("A"))
    }

    @Test
    fun startMarksExactlyThatValueInFlight() {
        val guard = InFlightValueGuard()
        guard.start("A")
        assertTrue(guard.isInFlight("A"), "the started value must report in flight (I-1)")
        assertFalse(guard.isInFlight("B"), "an unrelated value must not be blocked")
    }

    @Test
    fun settleClearsTheGuardSoARetryCanStart() {
        val guard = InFlightValueGuard()
        guard.start("A")
        guard.settle("A")
        assertFalse(guard.isInFlight("A"), "settle (completion OR failure) must clear the marker (I-2)")
        guard.start("A")
        assertTrue(guard.isInFlight("A"), "a retry for the same value must be trackable again")
    }

    @Test
    fun concurrentValuesAreTrackedIndependently() {
        val guard = InFlightValueGuard()
        guard.start("A")
        guard.start("B")
        // ACS-01 regression (A→B→A): while A's original load is still outstanding, flipping the
        // selection back to A must still report A in flight — otherwise a duplicate A load fires.
        assertTrue(guard.isInFlight("A"), "an outstanding load must stay tracked when another value starts (I-1)")
        assertTrue(guard.isInFlight("B"))
        guard.settle("A")
        assertFalse(guard.isInFlight("A"))
        assertTrue(guard.isInFlight("B"), "settling one value must not unblock another (per-value I-2)")
        guard.settle("B")
        assertFalse(guard.isInFlight("B"))
    }
}
