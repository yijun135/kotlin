// TARGET_BACKEND: JVM
// WITH_RUNTIME
// FILE: test.kt
// LANGUAGE_VERSION: 1.2
var component1Evaluated = false

inline fun <reified T> shouldThrow(block: () -> Unit) {
    try {
        block()
        throw AssertionError("Should throw")
    }
    catch (e: Throwable) {
        if (e !is T) throw e
    }
}

// NB extension receiver is nullable
operator fun J?.component1() = 1.also { component1Evaluated = true }

private operator fun J.component2() = 2

fun use(x: Any) {}

fun box(): String {
    shouldThrow<IllegalStateException> {
        val (a, b) = J.j()
    }
    if (!component1Evaluated) return "component1 should be evaluated"
    return "OK"
}


// FILE: J.java
public class J {
    public static J j() { return null; }
}