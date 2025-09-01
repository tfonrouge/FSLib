package com.fonrouge.utils.services

import com.fonrouge.base.state.SimpleState

interface IApiDataMediaService {
    suspend fun updateOrder(id: String, order: String?): SimpleState
}
