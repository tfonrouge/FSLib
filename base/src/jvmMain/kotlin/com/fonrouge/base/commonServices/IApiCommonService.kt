package com.fonrouge.base.commonServices

import io.ktor.server.application.*

/**
 * Common interface for API services to enable handling of HTTP application calls.
 */
@Suppress(
    "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "NON_ACTUAL_MEMBER_DECLARED_IN_EXPECT_NON_FINAL_CLASSIFIER_ACTUALIZATION_WARNING"
)
actual interface IApiCommonService {
    val call: ApplicationCall
}
