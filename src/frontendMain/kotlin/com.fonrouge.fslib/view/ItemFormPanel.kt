package com.fonrouge.fslib.view

import com.fonrouge.fslib.apiLib.KVWebManager.configViewListMap
import com.fonrouge.fslib.apiLib.KVWebManager.restContainerItem
import com.fonrouge.fslib.model.base.BaseModel
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.form.*
import io.kvision.form.select.Select
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class ItemFormPanel<T : BaseModel<*>>(
    val item: T?,
    method: FormMethod?,
    action: String?,
    enctype: FormEnctype?,
    type: FormType?,
    condensed: Boolean,
    horizRatio: FormHorizontalRatio,
    className: String?,
    serializer: KSerializer<T>?,
    customSerializers: Map<KClass<*>, KSerializer<*>>?,
) : FormPanel<T>(method, action, enctype, type, condensed, horizRatio, className, serializer, customSerializers) {

    val selectAjaxList = arrayListOf<SelectAjax<*>>()

    @Suppress("unused")
    inline fun <C : StringFormControl, reified K : BaseModel<*>> C.bindWithAjax(
        key: KProperty1<T, K?>, required: Boolean = false, requiredMessage: String? = null,
        selectKClass: KClass<K>,
        selectKPropText: KProperty1<K, String?>,
        selectedItemId: String? = null,
        layoutType: FormType? = null,
        noinline validatorMessage: ((C) -> String?)? = null,
        noinline validator: ((C) -> Boolean?)? = null,
        noinline onChange: ((K?) -> Unit)? = null,
    ): C {

        (this as Select).apply {

            val selectAjax = SelectAjax(this, key, selectKClass, selectKPropText)
            selectAjaxList.add(selectAjax)

            item?.let { key.get(it) }?.let { selectedItem ->
                if (key is KMutableProperty1) {
                    key.setValue(item, key, selectedItem)
                }
                val first = key.get(item)?.id?.toString() ?: ""
                selectAjax.selectedPair = first to selectKPropText.get(selectedItem)
            }

            if (selectedItemId != null) {
                restContainerItem(selectKClass, selectedItemId) { selectItem ->
                    selectItem?.let {
                        selectAjax.selectedPair = it.id.toString() to selectKPropText.get(it)
                    }
                }
            }

            ajaxOptions = configViewListMap[selectKClass.simpleName]?.ajaxOptions(selectKPropText).also {
                if (it == null) {
                    Toast.error(
                        message = "no ajaxOptions found for property ':${key.name}' in target class '${selectKClass.simpleName}'",
                        title = "Form Panel error",
                        options = ToastOptions(positionClass = ToastPosition.BOTTOMFULLWIDTH)
                    )
                }
            }

            onEvent {
                change = {
                    restContainerItem(selectKClass, self.value) { k ->
                        selectAjax.selectedPair = self.value to k.asDynamic()[selectKPropText.name]
                        onChange?.let { it(k) }
                        selectAjaxList.forEach { selCont ->
                            if (selCont.selectKClass.simpleName == selectKClass.simpleName && selCont.key.name == key.name) {
                                if (this@apply != selCont.select) {
                                    val pair =
                                        k.asDynamic()["id"] as? String to k.asDynamic()[selCont.selectKPropText.name] as? String
                                    selCont.selectedPair = pair
                                    selCont.select.value = pair.first
                                    selCont.select.selectedLabel = pair.second
                                }
                            }
                        }
                    }
                }
            }
        }

        bindCustom(key, required, requiredMessage, layoutType, validatorMessage, validator)

        return this
    }

    class SelectAjax<K : BaseModel<*>>(
        val select: Select,
        val key: KProperty1<*, K?>,
        val selectKClass: KClass<K>,
        val selectKPropText: KProperty1<K, String?>,
    ) {
        var selectedPair: Pair<String?, String?>? = null
    }

    companion object {
        inline fun <reified K : BaseModel<*>> create(
            item: K?,
            method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
            type: FormType? = null, condensed: Boolean = false,
            horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2, className: String? = null,
            customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
            noinline init: (ItemFormPanel<K>.() -> Unit)? = null,
        ): ItemFormPanel<K> {
            val formPanel =
                ItemFormPanel(
                    item,
                    method,
                    action,
                    enctype,
                    type,
                    condensed,
                    horizRatio,
                    className,
                    serializer(),
                    customSerializers
                )
            init?.invoke(formPanel)
            return formPanel
        }
    }
}

@Suppress("unused")
inline fun <reified K : BaseModel<*>> Container.itemFormPanel(
    item: K?,
    method: FormMethod? = null, action: String? = null, enctype: FormEnctype? = null,
    type: FormType? = null, condensed: Boolean = false,
    horizRatio: FormHorizontalRatio = FormHorizontalRatio.RATIO_2,
    className: String? = null,
    customSerializers: Map<KClass<*>, KSerializer<*>>? = null,
    noinline init: (ItemFormPanel<K>.() -> Unit)? = null,
): ItemFormPanel<K> {
    val formPanel =
        ItemFormPanel.create(
            item,
            method,
            action,
            enctype,
            type,
            condensed,
            horizRatio,
            className,
            customSerializers
        )
    init?.invoke(formPanel)
    this.add(formPanel)
    return formPanel
}
