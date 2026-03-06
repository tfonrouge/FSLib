package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import io.ktor.server.application.*
import java.io.File

class HelpDocsService(val call: ApplicationCall) : IHelpDocsService {
    companion object {
        var helpDocsDir: File = File("help-docs")
    }

    override suspend fun getAvailableHelp(viewClassName: String): Set<HelpType> {
        val viewDir = File(helpDocsDir, viewClassName)
        return HelpType.entries.filter { File(viewDir, it.fileName).exists() }.toSet()
    }

    override suspend fun getHelpContent(viewClassName: String, helpType: HelpType): String {
        val file = File(helpDocsDir, "$viewClassName/${helpType.fileName}")
        return if (file.exists()) file.readText() else ""
    }
}
