package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.IfceWebAction
import com.fonrouge.fslib.apiLib.TypeView
import com.fonrouge.fslib.config.BaseConfigView

abstract class ViewHomeBase : View(
    configView = BaseConfigView(name = "HomeBase", url = "", label = "Home", typeView = TypeView.None),
    actionPage = { view -> IfceWebAction.HomePage(view as ViewHomeBase) }
)
