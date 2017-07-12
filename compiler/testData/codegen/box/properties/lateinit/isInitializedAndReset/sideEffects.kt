// TARGET_BACKEND: JVM
// WITH_RUNTIME

class Foo {
    lateinit var bar: String

    fun test(): String {
        var state = 0
        if (run { state++; this }::bar.isInitialized) return "Fail 1"

        bar = "A"
        if (!run { state++; this }::bar.isInitialized) return "Fail 3"
        run { state++; this }::bar.reset()
        if (run { state++; this }::bar.isInitialized) return "Fail 4"

        return if (state == 4) "OK" else "Fail: state=$state"
    }
}

fun box(): String {
    return Foo().test()
}
