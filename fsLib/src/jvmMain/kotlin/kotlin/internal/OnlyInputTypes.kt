package kotlin.internal

/**
 * This annotation is used to specify constraints on type parameters, restricting them to input types only.
 *
 * It is primarily useful for scenarios involving generics where certain operations or transformations
 * must only be applied to input types. The annotation can ensure type safety and prevent unintended usage
 * or errors in such cases.
 *
 * When applied, it enforces the constraint during compilation, which aids in providing clearer
 * and more reliable code adherence to the intended design.
 *
 * This annotation is retained in the binary output for use cases where runtime reflection or
 * analysis might be needed.
 */
@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class OnlyInputTypes
