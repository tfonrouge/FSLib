package com.fonrouge.fullStack.layout

import io.kvision.panel.Tab
import io.kvision.panel.TabPanel
import io.kvision.panel.tab

/**
 * Adds a tab with the label "Main" and a house icon to the TabPanel.
 *
 * @param init A block of initialization code to configure the tab.
 * @return The created Tab instance.
 */
@Suppress("unused")
fun TabPanel.mainTab(init: Tab.() -> Unit): Tab = tab(label = "Main", icon = "fas fa-house", init = init)
