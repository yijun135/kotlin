// TARGET_BACKEND: JVM
// WITH_RUNTIME

class Foo {
    lateinit var bar: String

    fun test(): String {
        if (this::bar.isInitialized) return "Fail 1"
        this::bar.reset()
        if (this::bar.isInitialized) return "Fail 2"

        bar = "A"
        if (!this::bar.isInitialized) return "Fail 3"
        this::bar.reset()
        if (this::bar.isInitialized) return "Fail 4"

        bar = "OK"
        if (!this::bar.isInitialized) return "Fail 5"
        return bar
    }
}

fun box(): String {
    return Foo().test()
}
