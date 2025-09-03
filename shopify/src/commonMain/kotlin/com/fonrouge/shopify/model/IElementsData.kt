package com.fonrouge.shopify.model

interface IElementsData<T> {
    val elements: ElementConnection<T>
}
