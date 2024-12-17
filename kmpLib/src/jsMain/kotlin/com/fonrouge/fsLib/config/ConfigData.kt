package com.fonrouge.fsLib.config

import com.fonrouge.fsLib.model.apiData.IApiFilter

/**
 * Abstract class representing configuration data used by the application.
 *
 * This class is designed to hold a common container that implements the `ICommon` interface and associates it with a filter type.
 * It serves as a foundational construct for managing application views and their configuration.
 *
 * @param CC The type of the common container which must extend `ICommon`.
 * @param FILT The type of the filter utilized, extending the `IApiFilter` interface.
 * @property commonContainer The common container instance of type `CC` that holds and manages filters and other shared state.
 */
abstract class ConfigData<CC : ICommon<FILT>, FILT : IApiFilter<*>>(
    open val commonContainer: CC
)

/**
 * Constructs a configuration data instance tied to a specific common container implementing the `ICommon` interface
 * and associated filter of type `FILT`.
 *
 * @param commonContainer The common container instance of type `CC` used for managing filters and shared state.
 * @return An instance of [ConfigData] parameterized with the types `CC` and `FILT`, encapsulating the configuration details.
 */
fun <CC : ICommon<FILT>, FILT : IApiFilter<*>> configData(
    commonContainer: CC,
): ConfigData<CC, FILT> = object : ConfigData<CC, FILT>(commonContainer) {}
