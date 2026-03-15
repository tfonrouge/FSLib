package com.fonrouge.fullStack.config

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.view.ViewDataContainer
import kotlin.reflect.KClass

/**
 * An abstract class representing a configuration container for handling views of a specific type associated with a common container.
 *
 * @param T The type of items managed by the view, extending BaseDoc.
 * @param ID The type of the unique identifier for the items, which must be a non-nullable type.
 * @param V The type of the view data container associated with the configuration, extending ViewDataContainer.
 * @param FILT The type of the API filter used for querying, which must extend IApiFilter.
 * @param commonContainer The instance of the common container providing core data operations and labels.
 * @param viewKClass The KClass instance representing the type of the view data container.
 * @param baseUrl The optional base URL for API operations related to the configuration.
 */
abstract class ConfigViewContainer<T : BaseDoc<ID>, ID : Any, V : ViewDataContainer<T, ID, FILT>, FILT : IApiFilter<*>>(
    override val commonContainer: ICommonContainer<T, ID, FILT>,
    viewKClass: KClass<out V>,
    baseUrl: String? = null,
) : ConfigView<V, FILT>(
    commonContainer = commonContainer,
    viewKClass = viewKClass,
    _baseUrl = baseUrl,
) {
    enum class VMode {
        _blank,
        _self,
        _parent,
        _top,
        modal,
    }
}