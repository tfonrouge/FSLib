package com.fonrouge.base.model

/**
 * Interface defining user interface parameters related to session and inactivity configurations.
 *
 * This interface provides properties to manage user session settings and define
 * thresholds for inactivity before stopping automatic user interface updates.
 */
interface IUserSessionParams {
    /**
     * Represents the maximum duration in seconds of user inactivity before the UI ceases to refresh.
     *
     * This value may indicate the limit for inactivity in the user interface, beyond which
     * certain automated UI updates or refreshes are disabled. The value is nullable, meaning
     * it may not always be explicitly specified.
     */
    val inactivityUiSecsToNoRefresh: Int?

    /**
     * Specifies the maximum duration of a session in seconds.
     *
     * This value defines the total length of time in seconds a user session may remain active. It can be used
     * for session management and may help in enforcing session timeouts for security or resource
     * optimization purposes. The value is nullable, allowing for optional configuration.
     */
    val sessionMaxSecs: Int?
}
