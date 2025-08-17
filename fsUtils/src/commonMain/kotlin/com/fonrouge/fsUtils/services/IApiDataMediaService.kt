package com.fonrouge.fsUtils.services

import com.fonrouge.base.state.SimpleState

interface IApiDataMediaService {
    suspend fun updateOrder(id: String, order: String?): SimpleState
}
