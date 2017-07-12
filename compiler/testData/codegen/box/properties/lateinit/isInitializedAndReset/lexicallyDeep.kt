// TARGET_BACKEND: JVM
// WITH_RUNTIME

open class Foo {
    lateinit var bar: String

    private lateinit var baz: String

    fun test(): String {
        val isBarInitialized: () -> Boolean = { this::bar.isInitialized }
        if (isBarInitialized()) return "Fail 1"
        bar = "bar"
        if (!isBarInitialized()) return "Fail 2"

        baz = "baz"
        val reset = object {
            operator fun invoke() {
                class Local {
                    fun reset() {
                        this@Foo::bar.reset()
                        this@Foo::baz.reset()
                    }
                }
                Local().reset()
            }
        }
        reset()

        if (isBarInitialized()) return "Fail 3"

        if ({ this::baz.isInitialized }()) return "Fail 4"
        baz = "A"

        return InnerSubclass().testInner()
    }

    inner class InnerSubclass : Foo() {
        fun testInner(): String {
            // This is access to InnerSubclass.bar which is inherited from Foo.bar
            bar = "OK"
            if (!this::bar.isInitialized) return "Fail 5"

            // This is access to Foo.bar declared lexically above
            if (this@Foo::bar.isInitialized) return "Fail 6"
            return "OK"
        }
    }
}

fun box(): String {
    return Foo().test()
}
