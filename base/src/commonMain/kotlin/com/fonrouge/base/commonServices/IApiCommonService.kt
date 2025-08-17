package com.fonrouge.base.commonServices

/**
 * Represents a common service interface for API interactions.
 * This interface is designed to be implemented on different platforms with the `expect` keyword.
 *
 * It contains an `ApplicationCall` property which represents a call in the Ktor framework.
 *
 * Annotations:
 * - `Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")` suppresses warnings related to the `expect/actual` feature being in beta.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface IApiCommonService
