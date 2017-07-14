import kotlin.reflect.KProperty

class VD {
    <error descr="[INAPPLICABLE_OPERATOR_MODIFIER] 'operator' modifier is inapplicable on this function: second parameter must be of type KProperty<*> or its supertype">operator</error> fun getValue(thisRef: Any, property: KProperty<Int>): String = ""
}