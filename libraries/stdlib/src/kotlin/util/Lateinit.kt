@file:kotlin.jvm.JvmName("LateinitKt")
@file:kotlin.jvm.JvmVersion
@file:Suppress("unused")

package kotlin

import kotlin.internal.InlineOnly
import kotlin.internal.AccessibleLateinitPropertyLiteral
import kotlin.reflect.KProperty0

/**
 * Returns `true` if this lateinit property has been assigned a value, and `false` otherwise.
 */
// @SinceKotlin("1.2")
@InlineOnly
inline val @receiver:AccessibleLateinitPropertyLiteral KProperty0<*>.isInitialized: Boolean
    get() = throw NotImplementedError("Implementation is intrinsic")

/**
 * Resets the value of this lateinit property, making it non-initialized.
 */
// @SinceKotlin("1.2")
@InlineOnly
inline fun @receiver:AccessibleLateinitPropertyLiteral KProperty0<*>.reset() {
    throw NotImplementedError("Implementation is intrinsic")
}
