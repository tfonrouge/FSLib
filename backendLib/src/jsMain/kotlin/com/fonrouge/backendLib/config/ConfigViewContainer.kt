package com.fonrouge.backendLib.config

import com.fonrouge.fsLib.api.IApiFilter
import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.BaseDoc
import com.fonrouge.backendLib.view.ViewDataContainer
import kotlin.reflect.KClass

/**
 * An abstract class representing a configuration container for handling views of a specific type associated with a common container.
 *
 * @param CC The type of the common container that provides the underlying data management, extending ICommonContainer.
 * @param T The type of items managed by the view, extending BaseDoc.
 * @param ID The type of the unique identifier for the items, which must be a non-nullable type.
 * @param V The type of the view data container associated with the configuration, extending ViewDataContainer.
 * @param FILT The type of the API filter used for querying, which must extend IApiFilter.
 * @param commonContainer The instance of the common container providing core data operations and labels.
 * @param viewKClass The KClass instance representing the type of the view data container.
 * @param baseUrl The optional base URL for API operations related to the configuration.
 */
abstract class ConfigViewContainer<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<CC, T, ID, FILT>, FILT : IApiFilter<*>>(
    commonContainer: CC,
    viewKClass: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CC, V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    _baseUrl = baseUrl,
)