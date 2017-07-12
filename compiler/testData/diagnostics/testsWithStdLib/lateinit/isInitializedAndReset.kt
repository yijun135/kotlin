// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: test.kt

interface Base {
    var x: String
}

open class Foo : Base {
    override lateinit var x: String
    private lateinit var y: String

    var nonLateInit: Int = 1

    fun correct1() {
        val b: Boolean = this::x.isInitialized
        this::x.reset()

        val otherInstance = Foo()
        otherInstance::x.isInitialized
        Foo()::x.reset()
        Foo()::y.reset()

        (this::x).isInitialized
        (@Suppress("ALL") (this::x)).isInitialized

        val inLambda = { this::x.reset() }
        object {
            fun local() {
                class Local {
                    val xx = this@Foo::x.isInitialized
                    val yy = this@Foo::y.isInitialized
                }
            }
        }
    }

    fun incorrect1() {
        val p = this::x
        p.<!LATEINIT_INTRINSIC_CALL_ON_NON_LITERAL!>isInitialized<!>

        this::nonLateInit.<!LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT!>isInitialized<!>
    }

    inner class InnerSubclass : Foo() {
        fun correct2() {
            // This is access to Foo.x declared lexically above
            this@Foo::x.isInitialized

            // This is access to InnerSubclass.x which is inherited from Foo.x
            this::x.isInitialized
        }
    }
}

fun incorrect2() {
    Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>reset<!>()
    Foo()::nonLateInit.<!LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT!>isInitialized<!>
    Foo()::nonLateInit.<!LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT!>reset<!>()
}

object Unrelated {
    fun incorrect3() {
        Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    }
}

class FooImpl : Foo() {
    fun incorrect4() {
        this::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
        Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>reset<!>()
    }
}

// FILE: other.kt

class OtherFooImpl : Foo() {
    fun incorrect5() {
        this::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    }
}
