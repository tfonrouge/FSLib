package com.fonrouge.fsLib.view

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.common.callItemService
import com.fonrouge.fsLib.common.getItemState
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.layout.centeredMessage
import com.fonrouge.fsLib.lib.UrlParams
import com.fonrouge.fsLib.lib.toEncodedUrlString
import com.fonrouge.fsLib.lib.toast
import com.fonrouge.fsLib.model.apiData.CallType
import com.fonrouge.fsLib.model.apiData.CrudTask
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import com.fonrouge.fsLib.model.state.ItemState
import com.fonrouge.fsLib.model.state.SimpleState
import com.fonrouge.fsLib.model.state.State
import io.kvision.core.*
import io.kvision.form.DateFormControl
import io.kvision.form.FormControl
import io.kvision.form.FormPanel
import io.kvision.form.KFilesFormControl
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.flexPanel
import io.kvision.panel.vPanel
import io.kvision.state.ObservableList
import io.kvision.state.ObservableValue
import io.kvision.tabulator.Tabulator
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import io.kvision.utils.Serialization
import io.kvision.utils.em
import js.date.Date
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlinx.serialization.serializer
import org.w3c.dom.events.MouseEvent
import web.prompts.confirm
import kotlin.collections.set
import kotlin.js.json
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents a configurable view item that connects and interacts with the backend API
 * and provides functionalities to handle data viewing, editing, and validation.
 *
 * This class is designed for creating and managing UI forms that interact with backend
 * services via API calls, allowing CRUD (Create, Read, Update, Delete) actions.
 *
 * @param CC The type of the common container that holds the domain-specific logic and configuration.
 * @param T The type of the domain entity being managed.
 * @param ID The type of the identifier for the domain entity.
 * @param FILT The type of API filter used for querying or filtering data.
 * @param configView Configuration details for the view item, includes serializers and endpoint-related logic.
 * @param periodicUpdateDataView Determines if periodic updates of the view's data are allowed.
 * @param debug Enables or disables debugging for the instance.
 */
@Suppress("unused")
abstract class ViewItem<out CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>>(
    final override val configView: ConfigViewItem<CC, T, ID, out ViewItem<CC, T, ID, FILT>, FILT, *>,
    periodicUpdateDataView: Boolean? = null,
    private var debug: Boolean = false
) : ViewDataContainer<CC, T, ID, FILT>(
    configViewContainer = configView,
) {
    var buttonBack: Button? = null
    var buttonCancel: Button? = null
    var buttonAccept: Button? = null

    /**
     * Holds a mutable mapping of property references of type [KProperty1] to optional string representations.
     * Used to store custom mappings of data fields to their serialized or stringified values.
     * This can be used in data transformation operations or for managing dynamic configurations.
     */
    val customMapValues = mutableMapOf<String, CustomMapValue<*, *>>()

    /**
     * Holds a reference to a FormPanel instance that is used to manage the display
     * and handling of form inputs for a specified data type.
     *
     * The formPanel is initialized with a serializer to enable data serialization
     * for the associated form items.
     *
     * @property formPanel A dynamically typed FormPanel that supports interaction
     * with forms.
     */
    var formPanel: FormPanel<T> = FormPanel(serializer = configView.commonContainer.itemSerializer)

    /**
     * Observable that holds the [ItemState] for the [ViewItem]
     */
    val itemObservable: ObservableValue<T?> = ObservableValue(null)

    /**
     * Helper to get the item property from the [ItemState]
     */
    var item: T?
        get() = itemObservable.value
        set(value) {
            itemObservable.value = value
        }

    init {
        itemObservable.subscribe {
            it?.let { item ->
                if (debug) {
                    console.warn("itemObservable.subscribe:", item)
                }
                formPanel.setData(item)
                if (mainView) updateTitle()
                onChangeItemObservable(it)
            }
        }
    }

    /**
     * Represents the label of the view item, composed dynamically using the `configView`'s label and
     * the label ID of the current item from the common container.
     *
     * This label is utilized to provide a concise and descriptive textual representation, aiding in
     * UI rendering or internal debugging processes.
     */
    override val label: String
        get() = configView.commonContainer.labelItemId(item)

    /**
     * Provides the identifier string for the label associated with the current item in the view.
     * This identifier is determined dynamically based on the `commonContainer` configuration
     * and the current `item` in the `configView`.
     *
     * The property is utilized for associating a label with a corresponding item within the user interface
     * or backend system, facilitating structure and accessibility.
     */
    val labelId get() = configView.commonContainer.labelId(item)

    /**
     * Indicates whether the back button should be hidden or disabled in the current view context.
     *
     * This variable determines the visibility or functionality of the navigational back button.
     * It is primarily used to control user navigation within the interface. When set to `true`,
     * the back button is effectively deactivated or not shown. The default value is `false`,
     * which means the back button is enabled and visible.
     */
    var noBackButton = false

    /**
     * A callback function that is invoked when the accept button is clicked.
     * The function can handle custom logic or UI updates when the event occurs.
     *
     * This callback receives a reference to the button (`Button`) that was clicked
     * and the associated `MouseEvent` representing the click action.
     *
     * It can be used to define specific behavior for the accept button,
     * such as processing form submissions or triggering additional actions.
     *
     * The callback is optional and can be set to `null` if no specific action
     * needs to be performed on the button click.
     */
    var onAcceptButtonClick: (Button.(MouseEvent) -> Unit)? = null
    private var origSerialized: String? = null

    /**
     * Indicates whether periodic updates for the data view are enabled. If not explicitly set,
     * the value defaults to the `periodicUpdateDataViewItem` from `KVWebManager`.
     *
     * This property can be used to control or interrogate the state of periodic updates
     * within the `ViewItem` context.
     */
    final override var periodicUpdateDataView: Boolean? = periodicUpdateDataView
        get() = field ?: KVWebManager.periodicUpdateDataViewItem

    val tabulators: MutableMap<String, TabulatorItem<*>> = mutableMapOf()

    /**
     * A nullable map property that stores key-value pairs where keys are of type String and values are nullable Strings.
     * It is used to manage and hold configuration or form field data within the class. The map can be null, and its contents
     * are modifiable only within the owning class.
     *
     * This property is private for setting, ensuring controlled access and consistency when updating its data.
     */
    var valueMap: Map<String, String?> = emptyMap()
        private set

    data class CustomMapValue<F : FormControl, V>(
        val formControl: F,
        val toControlValue: (V?) -> String?,
        var serialized: String? = null
    ) {
        fun setValue(value: V?) {
            toControlValue(value)?.let { formControl.setValue(it) } ?: formControl.setValue(null)
        }
    }

    data class TabulatorItem<T : Any>(
        val tabulator: Tabulator<T>,
        val kClass: KClass<T>
    ) {
        @OptIn(InternalSerializationApi::class)
        fun toPlainObj(): dynamic {
            val x: List<T>? = tabulator.getData()
            val s = ListSerializer(kClass.serializer())
            return JSON.parse(Json.encodeToString(s, x as List<T>))
        }
    }

    /**
     * Executes an "upsert" action (either update or insert) for the current item, using form validation,
     * data transformation, and API service calls. Optionally displays toast notifications and updates UI components.
     *
     * @param block An optional lambda function to handle the result of the upsert API call. The function receives
     *              an [ItemState] parameter, which contains information about the success, error, or status of the operation.
     */
    fun acceptUpsertAction(
        block: ((ItemState<T>) -> Unit)? = {
            navButtonCancel?.hide()
            navButtonAccept?.hide()
            navButtonBack?.show()
            buttonCancel?.hide()
            buttonAccept?.hide()
            buttonBack?.show()
            val toastOptions = ToastOptions(
                callback = { backCloseAction() },
                close = true,
                stopOnFocus = true
            )
            if (it.hasError.not()) {
                Toast.info(
                    message = if (it.noDataModified == true) "No data was modified ..." else it.msgOk
                        ?: "info...",
                    options = toastOptions
                )
            } else {
                Toast.warning(
                    message = it.msgError ?: "!",
                    options = toastOptions
                )
            }
        }
    ) {
        val crudAction = crudTask
        if (crudAction != null && crudAction in arrayOf(CrudTask.Create, CrudTask.Update)) {
            if (formPanel.validate()) {
                val data = transformData(getData())
                val simpleState = formPanelValidate(data)
                if (simpleState.state == State.Ok) {
                    configView.commonContainer.callItemService(
                        apiItemFun = configView.apiItemFun,
                        crudTask = crudAction,
                        callType = CallType.Action,
                        id = item?._id,
                        item = data,
                        orig = origSerialized?.let {
                            Json.decodeFromString(
                                deserializer = configView.commonContainer.itemSerializer,
                                string = it
                            )
                        },
                        apiFilter = apiFilter,
                    ) { itemResponse ->
                        block?.invoke(itemResponse)
                        if (crudAction == CrudTask.Update && itemResponse.hasError.not()) {
                            origSerialized = Json.encodeToString(
                                serializer = configView.commonContainer.itemSerializer,
                                value = data
                            )
                        }
                        itemResponse
                    }
                } else {
                    simpleState.toast()
                }
            } else {
                Toast.warning(
                    message = "Form has incomplete data",
                    options = ToastOptions(
                        position = ToastPosition.BOTTOMRIGHT,
                        stopOnFocus = true
                    )
                )
            }
        }
    }

    /**
     * Adds a viewList to the container and optionally initializes it with custom logic.
     *
     * @param viewList The ViewList object to be added. It should extend from ICommonContainer and support an API filter implementation.
     * @param init An optional lambda function to initialize the ViewList with custom configurations or behavior.
     */
    @Suppress("unused")
    fun Container.addViewList(
        viewList: ViewList<ICommonContainer<*, *, out IApiFilter<ID>>, *, *, out IApiFilter<ID>, ID>,
        init: ((ViewList<*, *, *, *, *>).() -> Unit)? = null
    ) {
        viewList.apply { startDisplayPage() }
        viewList.masterViewItem = this@ViewItem
        init?.invoke(viewList)
    }

    /**
     * Handles the back or close action for the current view. If confirmation is required
     * and unsaved changes are detected, prompts the user for confirmation before proceeding.
     * Depending on the browser history, navigates back or closes the window.
     *
     * @param confirmCancel Indicates if the user should be prompted to confirm canceling any unsaved changes.
     *                      If true, the method detects changes in the form panel data and compares them
     *                      with the original item state. If changes are detected, a confirmation dialog
     *                      is displayed to the user.
     */
    fun backCloseAction(confirmCancel: Boolean = false) {
        var proceedClose = true
        if (confirmCancel) {
            try {
                val s1 = Json.encodeToString(
                    configView.commonContainer.itemSerializer,
                    transformData(getData())
                )
                val s2 =
                    item?.let { Json.encodeToString(configView.commonContainer.itemSerializer, it) }
                if (s1 != s2) {
                    proceedClose = confirm("Cancel and forget current changes ?")
                }
            } catch (e: Exception) {
                console.warn("exception = ", e)
            }
        }
        if (proceedClose) {
            if (window.history.length > 1) {
                window.history.back()
            } else {
                window.close()
            }
        }
    }

    /**
     * Binds the given property to the provided observable list and associates it with the tabulator.
     *
     * @param property The property of type `KProperty1` that refers to a collection of items to bind.
     * @param data The observable list that will be populated with the values from the provided property.
     *      This data can be assigned to the Tabulator data property
     * @return The current instance of the `Tabulator` to allow for method chaining.
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified V : Any> Tabulator<V>.bind(
        property: KProperty1<in T, Collection<V>>,
        data: ObservableList<V>
    ): Tabulator<V> {
        item?.let { property.get(it) }?.let { x ->
            data.addAll(x)
        }
        tabulators[property.name] = TabulatorItem(this, V::class)
        return this
    }

    /**
     * Binds a custom value from a property to a form control, allowing transformation of the property value
     * into a form control-compatible representation.
     *
     * @param property The property from which the value is to be obtained.
     * @param toControlValue A transformation function that converts the property's value to a string compatible with the control.
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <F : FormControl, reified V> F.bindCustomValue(
        property: KProperty1<in T, V?>,
        noinline toControlValue: (V?) -> String?,
    ) {
        customMapValues[property.name] = CustomMapValue(
            formControl = this,
            toControlValue = toControlValue,
            serialized = item?.let { property.get(it) }?.let { it: V? -> Json.encodeToString(it) }
        )
        setValue(toControlValue(item?.let { property.get(it) }))
    }


    /**
     * Updates the data in the view based on the current CRUD task.
     *
     * Specifically, if the current task is a "Read" operation, this method retrieves the item
     * based on its ID, fetches the corresponding state from the API through the configured
     * query function, and updates the observable item with the retrieved data.
     *
     * Behavior:
     * - Checks if the task is set to `Read` in the current CRUD operation.
     * - If the `item` has an associated ID, the method calls the API function `apiItemQueryRead`
     *   with the item ID and filter settings.
     * - Updates the state of the observable item (`itemObservable`) with the fetched data
     *   to reflect the current state in the UI.
     */
    final override fun dataUpdate() {
        if (crudTask == CrudTask.Read) {
            item?._id?.let { id ->
                configView.commonContainer.getItemState(
                    apiItemFun = configView.apiItemFun,
                    apiItem = configView.commonContainer.apiItemQueryRead(id = id, apiFilter = apiFilter),
                ) {
                    itemObservable.value = it.item
                }
            }
        }
    }

    /**
     * Displays a default message in the container, typically used when no specific CRUD action
     * or default behavior is defined for the context.
     *
     * @param urlParams An optional parameter containing URL-specific information that may
     *                  influence the displayed default message or behavior.
     */
    open fun Container.displayDefault(urlParams: UrlParams?) {
        centeredMessage("no CRUD action ...")
    }

    /**
     * Displays a form in the container based on the specified CRUD operation. The form can be customized
     * to handle Create, Read, or Update tasks, and includes options for action buttons such as back, cancel,
     * and accept, depending on the provided task and the application's state.
     *
     * @param crudTask The CRUD operation context (e.g., Create, Read, Update) for which the form is displayed.
     *                 This determines the behavior and data handling of the form.
     */
    private suspend fun Container.displayForm(crudTask: CrudTask) {
        onBeforeDisplayForm(crudTask)
        formPanel = pageItemBody()
        if (!actionUpsert) {
            formPanel.form.fields.forEach { entry ->
                entry.value.disabled = true
            }
        }
        flexPanel(direction = FlexDirection.ROW, justify = JustifyContent.CENTER, spacing = 20) {
            marginTop = 1.em
            if (actionUpsert) {
                buttonBack =
                    button(
                        text = "Back",
                        icon = "fas fa-reply",
                        style = ButtonStyle.OUTLINEPRIMARY
                    ) {
                        hide()
                        onClick {
                            backCloseAction()
                        }
                    }
                buttonCancel =
                    button(
                        text = "Cancel",
                        icon = "fas fa-xmark",
                        style = ButtonStyle.OUTLINEDANGER
                    ) {
                        onClick {
                            backCloseAction(confirmCancel = true)
                        }
                    }
                buttonAccept =
                    button(
                        text = "Accept",
                        icon = "fas fa-check",
                        style = ButtonStyle.OUTLINESUCCESS
                    ) {
                        onClick {
                            acceptUpsertAction()
                        }
                    }
            } else {
                if (!noBackButton) {
                    val histLength = window.history.length
                    val label = if (histLength > 1) "Back" else "Close"
                    button(text = label, icon = "fa-solid fa-arrow-rotate-left").onClick {
                        backCloseAction()
                    }
                }
            }
        }
        when (crudTask) {
            CrudTask.Create -> {
                item?.let {
                    formPanel.setData(it)
                } ?: if (valueMap.isNotEmpty()) {
                    formSetDataWithValueMap(valueMap)
                } else Unit
            }

            CrudTask.Read -> {
                item?.let {
                    formPanel.setData(it)
                }
                installUpdate()
            }

            CrudTask.Update -> {
//                AppScope.launch {
//                    linkBanner?.label = labelBanner(apiFilter)
//                }
                item?.let {
                    formPanel.setData(it)
                    origSerialized = Json.encodeToString(
                        configView.commonContainer.itemSerializer,
                        transformData(getData())
                    )
                }
            }

            else -> {}
        }

        onAfterDisplayForm(crudTask)
    }

    /**
     * Displays a page in the container by rendering a user interface based on the given URL parameters, page context, and CRUD task.
     * The method handles Create, Read, Update, and Delete actions, manages API calls, and updates the UI accordingly.
     * It also manages navigation controls, confirmation dialogs, toast notifications, and form rendering.
     *
     * Behavior:
     * - Displays a page banner if enabled (`noPageBanner` is false).
     * - Handles different CRUD tasks (e.g., creating, updating, deleting) by interacting with necessary services and rendering the appropriate UI.
     * - Displays confirmation dialogs for delete actions.
     * - Calls API services for CRUD operations, updates item data, and transitions between Create to Update, if necessary.
     * - Fires UI updates and displays forms with custom actions using asynchronous flow.
     * - Manages navigation interactions including `back` actions and error handling.
     *
     * Notes:
     * 1. If no specific CRUD task (`crudTask`) is set, it displays a default page.
     * 2. For Create actions, the method checks if an item already exists and switches to an Update context if necessary.
     * 3. Encodes and decodes item IDs using defined serializers for URL interactions.
     */
    final override fun Container.displayPage() {
        vPanel(className = "showItem") {
            flexPanel(direction = FlexDirection.COLUMN, spacing = 10) {
                if (!noPageBanner) {
                    pageBanner()
                }
                crudTask?.let { crudAction ->
                    if (crudAction == CrudTask.Delete) {
                        item?.let { item ->
                            confirmDeleteView(
                                apiItemFun = configView.apiItemFun,
                                item = item,
                                apiFilter = apiFilter
                            )
                        } ?: Toast.danger("${configView.commonContainer.labelItem} not valid ...")
                    } else {
                        configView.commonContainer.callItemService(
                            apiItemFun = configView.apiItemFun,
                            crudTask = crudAction,
                            callType = CallType.Query,
                            id = urlParams.id?.let {
                                Json.decodeFromString(
                                    configView.commonContainer.idSerializer,
                                    it
                                )
                            },
                            apiFilter = apiFilter
                        ) { itemResponse ->
                            if (crudAction == CrudTask.Create) {
                                if (itemResponse.item != null) {
                                    if (itemResponse.itemAlreadyOn) {
                                        crudTask = CrudTask.Update
                                        urlParams.params["action"] = CrudTask.Update.name
                                        itemResponse.item._id.let {
                                            urlParams.params.set(
                                                propertyName = "id",
                                                value = Json.encodeToString(
                                                    configView.commonContainer.idSerializer,
                                                    it
                                                )
                                            )
                                        }
                                        @Suppress("UNUSED_VARIABLE")
                                        val url =
                                            (configView.url + urlParams.toEncodedUrlString()).asDynamic()

                                        @Suppress("UNUSED_VARIABLE")
                                        val stateObj =
                                            "{${itemResponse::class.simpleName}: \"${itemResponse.item._id}\"}".asDynamic()
                                        js("""history.replaceState(stateObj,"createToUpdate",url)""")
                                    }
                                } else if (itemResponse.valueMap != null) {
                                    valueMap = itemResponse.valueMap
                                }
                            }
                            var alreadyBack = false
                            val toastOptions = ToastOptions(
                                position = ToastPosition.BOTTOMRIGHT,
                                stopOnFocus = true,
                                duration = 10000,
                                close = true,
                                callback = {
                                    if (!alreadyBack) js("history.back()")
                                    Unit
                                },
                                escapeHtml = true,
                            )
                            val crudAction1 = crudTask
                            if (itemResponse.hasError.not() && crudAction1 != null) {
                                itemObservable.value = itemResponse.item
                                AppScope.launch {
                                    displayForm(crudAction1)
                                }
                            } else {
                                flexPanel(
                                    direction = FlexDirection.COLUMN,
                                    justify = JustifyContent.CENTER,
                                    alignContent = AlignContent.CENTER,
                                    alignItems = AlignItems.CENTER,
                                    spacing = 10
                                ) {
                                    div(
                                        content = "<i><b>[$crudAction1]</b></i> action denied: <b>${itemResponse.msgError}</b>",
                                        rich = true
                                    ) {
                                        fontSize = 1.5.em
                                    }
                                    flexPanel(
                                        direction = FlexDirection.ROW,
                                        justify = JustifyContent.CENTER,
                                        spacing = 20
                                    ) {
                                        button("Back", icon = "fa-solid fa-arrow-rotate-left") {
                                            onClick {
                                                alreadyBack = true
                                                js("history.back()") as? Unit
                                            }
                                        }
                                    }
                                }
                                Toast.warning(
                                    message = itemResponse.msgError
                                        ?: "$crudAction1 action denied ...",
                                    options = toastOptions
                                )
                            }
                            itemResponse
                        }
                    }
                } ?: displayDefault(urlParams)
            }
        }
    }

    /**
     * Encodes the given ID into a JSON string representation using the specified serializer.
     *
     * @param id The ID to be encoded. If null, the function returns null. Defaults to the `_id` property of the `item`.
     * @return A JSON string representation of the encoded ID, or null if the input ID is null.
     */
    fun encodeId(id: ID? = item?._id): String? {
        return id?.let { Json.encodeToString(configView.commonContainer.idSerializer, id) }
    }

    /**
     * Validates the form panel data.
     *
     * @param data The data to be validated.
     * @return A SimpleState object indicating the validation result.
     */
    open fun formPanelValidate(data: T?): SimpleState =
        SimpleState(
            isOk = data != null,
            msgError = "${configView.commonContainer.labelItem} is null"
        )

    /**
     * Populates form fields within the `formPanel` using data from the provided map.
     * For each entry in the map, updates the corresponding form field with parsed data.
     * Handles specific types of controls, such as `DateFormControl` and `KFilesFormControl`,
     * using custom parsing and value setting logic.
     *
     * @param map A map where the key represents the form field identifier and the value is its serialized data.
     *            Null values result in clearing the corresponding form field.
     */
    private fun formSetDataWithValueMap(map: Map<String, String?>) {
        map.forEach { entry ->
            formPanel.form.fields[entry.key]?.let { formControl ->
                entry.value?.let { value -> JSON.parse<Any>(value) }?.let { value ->
                    when (formControl) {
                        is DateFormControl -> formControl.value =
                            Date(value.unsafeCast<String>()).unsafeCast<kotlin.js.Date>()

                        is KFilesFormControl -> formControl.value = Serialization.plain.decodeFromString(
                            ListSerializer(KFile.serializer()),
                            JSON.stringify(value)
                        )

                        else -> formControl.setValue(value)
                    }
                } ?: formControl.setValue(null)
            }
        }
    }

    /**
     * Retrieves a custom value of type [V] for a specified property from the custom map values.
     *
     * This function attempts to find the serialized value in the `customMapValues` using the property's name
     * as the key. If a serialized value is found, it is deserialized into the specified type [V].
     *
     * @param property the property for which to retrieve the custom value
     * @return the custom value of type [V] if present and successfully deserialized, or null otherwise
     */
    inline fun <reified V> getCustomValue(property: KProperty1<in T, V?>): V? =
        customMapValues[property.name]?.serialized?.let {
            Json.decodeFromString(it)
        }

    /**
     * Retrieves data by merging the serialized form data and custom map values.
     *
     * The method serializes the form panel data, adds the custom map values
     * (if any), and deserializes the resulting object to produce the final data.
     *
     * @return The combined data of type T obtained after processing form panel data
     *         and custom map values.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun getData(): T {
        val item1 = formPanel.getData()
        if (valueMap.isEmpty() && tabulators.isEmpty()) return item1
        @Suppress("UnusedVariable") val s0 = Json.encodeToDynamic(configView.commonContainer.itemSerializer, item1)
        val s1 = json()
        valueMap.forEach { (key, value) ->
            s1[key] = value?.let { JSON.parse(it) }
        }
        customMapValues.forEach { (key: String, mapValue): Map.Entry<String, CustomMapValue<*, *>> ->
            s1[key] = mapValue.serialized?.let { JSON.parse(it) }
        }
        tabulators.forEach { (key: String, tabulatorItem: TabulatorItem<*>) ->
            s1[key] = tabulatorItem.toPlainObj()
        }
        val s2 = js("Object.assign({}, s0, s1)")
        val item2 = Json.decodeFromDynamic(configView.commonContainer.itemSerializer, s2)
        return item2
    }

    /**
     * Retrieves a TabulatorItem corresponding to the given property.
     *
     * @param property The property for which the TabulatorItem is to be retrieved.
     * @return The TabulatorItem associated with the specified property, or null if none exists.
     */
    @Suppress("UNCHECKED_CAST")
    fun <V : Any> getTabulator(property: KProperty1<in T, Collection<V>>): Tabulator<V>? =
        tabulators[property.name]?.tabulator as? Tabulator<V>?

    /**
     * Retrieves the tabulator value associated with the given property.
     *
     * @param property The property whose associated tabulator value is to be retrieved.
     * @return The tabulator value of the specified property, or null if no value is found.
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified V : Any> getTabulatorValue(
        property: KProperty1<in T, V>
    ): V? {
        return tabulators[property.name]?.tabulator?.getData() as V?
    }

    /**
     * This method is triggered after the form associated with the given CRUD task has been displayed.
     * It can be overridden to implement additional processing or actions specific to the form display context.
     *
     * @param crudTask The CRUD operation context (e.g., Create, Read, Update, Delete) for which the form is displayed.
     */
    open fun onAfterDisplayForm(crudTask: CrudTask) {}

    /**
     * This method is triggered before the form associated with the given CRUD task is displayed.
     * It can be overridden to perform custom initialization or setup actions before the form display.
     *
     * @param crudTask The CRUD operation context (e.g., Create, Read, Update, Delete) for which the form is about to be displayed.
     */
    open suspend fun onBeforeDisplayForm(crudTask: CrudTask) {}

    /**
     * Called when an observable item of type T changes. This method can be overridden to provide custom
     * behavior or handling for changes in an observably tracked item.
     *
     * @param item The item of type T that has changed. Use this parameter to access details or to react
     *             to updates in the item's state.
     */
    open fun onChangeItemObservable(item: T) {}

    /**
     * Builds and returns a FormPanel component within the current container.
     * This method is intended to be overridden in subclasses to provide custom
     * layout or UI elements for displaying or editing page items.
     *
     * @return A FormPanel of type T, which serves as the main container for the page item body.
     */
    abstract fun Container.pageItemBody(): FormPanel<T>

    /**
     * Sets a custom value for a given property. This function updates the value in the associated control
     * and serializes the given value into the custom map.
     *
     * @param property The property whose value needs to be updated.
     * @param value The new value to be set for the property.
     */
    inline fun <reified V> setCustomValue(property: KProperty1<in T, V?>, value: V?) {
        @Suppress("UNCHECKED_CAST")
        (customMapValues[property.name]?.toControlValue as ((V?) -> String?)?)?.let {
            customMapValues[property.name]?.formControl?.setValue(
                it(value)
            )
        }
        customMapValues[property.name]?.serialized = Json.encodeToString(value)
    }

    /**
     * Transforms the given input data and returns the transformed result.
     *
     * @param item The input data of type T to be transformed.
     * @return The transformed data of type T.
     */
    open fun transformData(item: T): T = item
}
