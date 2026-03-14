package com.example.showcase

import com.fonrouge.base.enums.HelpType
import com.fonrouge.fullStack.services.IHelpDocsService
import dev.kilua.rpc.annotations.RpcBindingRoute
import dev.kilua.rpc.annotations.RpcService

/**
 * RPC service interface for help documentation in the showcase sample.
 *
 * Methods must be redeclared with `override` so that KSP generates the client proxy
 * for inherited methods from [IHelpDocsService].
 */
@RpcService
interface IShowcaseHelpDocsService : IHelpDocsService {
    @RpcBindingRoute("IShowcaseHelpDocsService.getAvailableHelp")
    override suspend fun getAvailableHelp(viewClassName: String, moduleSlug: String?): Set<HelpType>
    @RpcBindingRoute("IShowcaseHelpDocsService.getHelpContent")
    override suspend fun getHelpContent(viewClassName: String, helpType: HelpType, moduleSlug: String?): String
}
