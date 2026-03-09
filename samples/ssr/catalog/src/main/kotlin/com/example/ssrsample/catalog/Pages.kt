package com.example.ssrsample.catalog

import com.fonrouge.ssr.PageDef

/**
 * CRUD page definition for [Product].
 * Defines list columns and form fields for product management.
 */
class ProductPage(
    repo: InMemoryRepository<CommonProduct, Product, ProductFilter>,
) : PageDef<CommonProduct, Product, String, ProductFilter>(
    commonContainer = CommonProduct,
    repository = repo,
    basePath = "/products",
) {
    init {
        column(Product::name, "Name") { sortable(); filterable() }
        column(Product::description, "Description")
        column(Product::price, "Price") { sortable() }
        column(Product::category, "Category") { filterable() }
        column(Product::inStock, "In Stock") { badge(mapOf("true" to "success", "false" to "secondary")) }

        field(Product::_id) { hidden() }
        field(Product::name, "Name") { required(); maxLength(100); col(6) }
        field(Product::description, "Description") { textarea(); col(12) }
        field(Product::price, "Price") { required(); number(); col(3) }
        field(Product::category, "Category") {
            select("Electronics", "Books", "Clothing", "Home", "Sports")
            col(3)
        }
        field(Product::inStock, "In Stock") { checkbox() }
    }

    override fun parseId(raw: String): String = raw
}

/**
 * CRUD page definition for [Customer].
 * Defines list columns and form fields for customer management.
 */
class CustomerPage(
    repo: InMemoryRepository<CommonCustomer, Customer, CustomerFilter>,
) : PageDef<CommonCustomer, Customer, String, CustomerFilter>(
    commonContainer = CommonCustomer,
    repository = repo,
    basePath = "/customers",
) {
    init {
        column(Customer::firstName, "First Name") { sortable() }
        column(Customer::lastName, "Last Name") { sortable() }
        column(Customer::email, "Email")
        column(Customer::city, "City") { filterable() }
        column(Customer::active, "Active") { badge(mapOf("true" to "success", "false" to "secondary")) }

        field(Customer::_id) { hidden() }
        field(Customer::firstName, "First Name") { required(); col(6) }
        field(Customer::lastName, "Last Name") { required(); col(6) }
        field(Customer::email, "Email") { required(); email(); col(6) }
        field(Customer::phone, "Phone") { col(6) }
        field(Customer::city, "City") { col(6) }
        field(Customer::active, "Active") { checkbox() }
    }

    override fun parseId(raw: String): String = raw
}
