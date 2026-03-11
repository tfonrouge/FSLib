package com.example.showcase

import com.fonrouge.fullStack.help.IHelpModule

/**
 * Help module grouping for the showcase sample.
 * Maps to the `help-docs/tasks/` directory.
 */
sealed class ShowcaseModule(
    override val slug: String,
    override val displayName: String,
) : IHelpModule {

    /** Task management module. */
    data object Tasks : ShowcaseModule("tasks", "Task Management")
}
