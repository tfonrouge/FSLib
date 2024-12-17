package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.ApiList
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ListState
import io.kvision.remote.KVServiceManager

/**
 * An abstract class representing a configuration data list that integrates various components like
 * service management, API operations, and filtering capabilities to handle and manage a collection of data items
 * associated with a specific configuration.
 *
 * @param CC The type of the common container that manages the collection of API items.
 *           It must extend ICommonContainer with specific type parameters.
 * @param T The type of the data items being managed, which must extend BaseDoc.
 * @param ID The type of the identifier field for the data items.
 * @param E The type of the service manager used for execution of API operations.
 * @param FILT The type of the API filter used for querying and filtering data, which must extend IApiFilter.
 * @param MID The type of the master item identifier used in the filter.
 * @property serviceManager The service manager to handle and execute API operations.
 * @property apiListFun A suspending function that performs an API list operation on a service manager instance
 *                      and returns a ListState containing the collection of data items.
 */
abstract class ConfigDataList<CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, E : Any, FILT : IApiFilter<MID>, MID : Any>(
    commonContainer: CC,
    val serviceManager: KVServiceManager<E>,
    val apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
) : ConfigData<CC, FILT>(commonContainer = commonContainer)

/**
 * Configures a data list component with the specified container, service manager, and API function for listing data.
 *
 * @param CC The type of the common container, which implements ICommonContainer.
 * @param T The type of the document or data item, which must extend BaseDoc.
 * @param ID The type of the ID of the document, which must be a non-nullable type.
 * @param E The type of the service manager entity.
 * @param FILT The type of the API filter, which must extend IApiFilter.
 * @param MID The type of the master item identifier used in the API filter.
 *
 * @param commonContainer The container managing the API items and configuration, implementing ICommonContainer.
 * @param serviceManager The service manager used for executing API logic and managing state.
 * @param apiListFun A suspend function representing the API logic to fetch a paginated list of items based on the given filter.
 *
 * @return An instance of ConfigDataList that centralizes management and configuration of data listings.
 */
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, E : Any, FILT : IApiFilter<MID>, MID : Any> configDataList(
    commonContainer: CC,
    serviceManager: KVServiceManager<E>,
    apiListFun: suspend E.(ApiList<FILT>) -> ListState<T>,
): ConfigDataList<CC, T, ID, E, FILT, MID> = object : ConfigDataList<CC, T, ID, E, FILT, MID>(
    commonContainer = commonContainer,
    serviceManager = serviceManager,
    apiListFun = apiListFun,
) {}
