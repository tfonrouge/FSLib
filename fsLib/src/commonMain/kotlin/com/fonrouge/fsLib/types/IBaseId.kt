package com.fonrouge.fsLib.types

/**
 * Represents a generic interface for defining an entity with an ID field.
 *
 * This interface is designed to provide a standard structure for associating an identifier
 * of a specified type with an entity. The generic type `ID` allows flexibility in defining
 * the type of the identifier, such as `Int`, `String`, or other types.
 *
 * Classes or data classes that implement this interface must override the `id` property,
 * ensuring type safety and consistency across different implementations.
 *
 * @param ID The type of the identifier. It must be non-nullable.
 * @property id The identifier of the entity.
 */
interface IBaseId<ID> where ID : Comparable<ID> {
    val id: ID
}
