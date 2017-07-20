println("Hello World!")

fun getBarOrNull(flag: Boolean): Bar? {
    return if (flag) Bar() else null
}

class Bar

println("Goodbye World!")
