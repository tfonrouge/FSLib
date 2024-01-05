package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

abstract class ICommonViewContainer<FILT : IApiFilter>(
    label: String,
) : ICommonView<FILT>(label = label)
