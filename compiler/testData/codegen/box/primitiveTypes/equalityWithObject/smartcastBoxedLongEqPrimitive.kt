val boxed: Any? = 42L

fun box(): String {
    if (boxed is Long) {
        if (boxed != 42L) return "Fail 1"
        if (boxed == 0L) return "Fail 2"
    }
    return "OK"
}