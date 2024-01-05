package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

abstract class CommonViewContainer<FILT : IApiFilter>(
    label: String,
) : CommonView<FILT>(label = label)
