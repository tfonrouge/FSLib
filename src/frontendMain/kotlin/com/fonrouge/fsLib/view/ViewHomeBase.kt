package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.apiLib.IfceWebAction
import com.fonrouge.fsLib.apiLib.TypeView
import com.fonrouge.fsLib.config.BaseConfigView

abstract class ViewHomeBase : View(
    configView = BaseConfigView(name = "HomeBase", url = "", label = "Home", typeView = TypeView.None),
    actionPage = { view -> IfceWebAction.HomePage(view as ViewHomeBase) }
)
