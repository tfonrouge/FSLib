package com.example.showcase

import com.fonrouge.fullStack.services.HelpDocsService
import io.ktor.server.application.*

/**
 * Concrete server-side help documentation service for the showcase sample.
 *
 * All logic is provided by the abstract [HelpDocsService] class; this subclass
 * only exists so that the `@RpcService` annotation (on [IShowcaseHelpDocsService])
 * triggers KSP proxy generation in this project's scope.
 */
class ShowcaseHelpDocsService(call: ApplicationCall) : HelpDocsService(call), IShowcaseHelpDocsService
