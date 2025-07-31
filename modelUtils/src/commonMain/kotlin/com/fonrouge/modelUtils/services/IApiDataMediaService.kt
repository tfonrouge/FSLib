package com.fonrouge.modelUtils.services

import com.fonrouge.fsLib.model.state.SimpleState

interface IApiDataMediaService {
    suspend fun updateOrder(id: String, order: String?): SimpleState
}
