package com.fonrouge.fsLib.apiServices

import io.ktor.server.application.*

@Suppress(
    "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "NON_ACTUAL_MEMBER_DECLARED_IN_EXPECT_NON_FINAL_CLASSIFIER_ACTUALIZATION_WARNING"
)
actual interface IApiService {
    val call: ApplicationCall
}
