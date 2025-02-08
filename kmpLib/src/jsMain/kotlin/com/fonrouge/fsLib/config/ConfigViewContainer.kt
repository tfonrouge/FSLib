package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.view.ViewDataContainer
import kotlin.reflect.KClass

/**
 * An abstract base class for configuring and managing a view container that connects
 * to a data model and handles view-related operations.
 *
 * This class serves as a specialized configuration container that combines the functionalities
 * of `ConfigView` and a generalized data container, providing means to handle data views
 * based on specific configurations, models, and filters.
 *
 * @param CC The type of the common container used for managing the data model, must extend `ICommonContainer`.
 * @param T The type of the document or data entity managed by the container, must extend `BaseDoc`.
 * @param ID The type of ID used to uniquely identify entities, required to be a non-nullable type.
 * @param V The type of the view container associated with this configuration, must extend `ViewDataContainer`.
 * @param FILT The type of the filter applied to the API queries and actions, must extend `IApiFilter`.
 * @param configData The configuration data used to initialize the container, contains metadata or settings.
 * @param viewKClass The Kotlin class reference for the view container type.
 * @param baseUrl Optional base URL used for defining API paths or external resource references.
 */
abstract class ConfigViewContainer<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<CC, T, ID, FILT>, FILT : IApiFilter<*>>(
    configData: ConfigData<CC, FILT>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<CC, V, FILT>(
    configData = configData,
    viewKClass = viewKClass,
    _baseUrl = baseUrl,
)
