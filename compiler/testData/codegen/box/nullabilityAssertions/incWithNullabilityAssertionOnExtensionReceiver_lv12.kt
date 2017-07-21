// TARGET_BACKEND: JVM
// FILE: test.kt
// WITH_RUNTIME
// LANGUAGE_VERSION: 1.2
operator fun A.inc() = A()

inline fun <reified T> shouldThrow(block: () -> Unit) {
    try {
        block()
        throw AssertionError("Should throw")
    }
    catch (e: Throwable) {
        if (e !is T) throw e
    }
}

fun box(): String {
    shouldThrow<IllegalArgumentException> {
        var aNull = A.n()
        aNull++
    }

    return "OK"
}

// FILE: A.java
public class A {
    public static A n() { return null; }
}