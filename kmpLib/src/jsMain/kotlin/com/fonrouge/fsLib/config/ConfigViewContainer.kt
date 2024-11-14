package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

/**
 * A container class for configuring views with a common container type.
 *
 * @param CC The type of the common container, which must extend ICommonContainer.
 * @param T The type of the items managed by the common container, which must extend BaseDoc.
 * @param ID The type of the ID field of the items, which must be a non-nullable type.
 * @param V The type of the view data container.
 * @param FILT The type of the API filter used for querying, must extend IApiFilter.
 * @property commonContainer An instance of the common container.
 * @constructor Initializes the ConfigViewContainer with the specified parameters.
 */
abstract class ConfigViewContainer<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<CC, T, ID, FILT>, FILT : IApiFilter<*>>(
    override val commonContainer: CC,
    viewKClass: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CC, V, FILT>(
    viewKClass = viewKClass,
    commonContainer = commonContainer,
    _baseUrl = baseUrl,
)
