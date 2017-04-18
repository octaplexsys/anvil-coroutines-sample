package adeln.coroutines

import com.squareup.moshi.Moshi
import okio.BufferedSource

fun <T : Any> Moshi.toJson(t: T): String =
        adapter(t.javaClass).toJson(t)

inline fun <reified T : Any> Moshi.fromJson(src: BufferedSource): T =
        adapter(T::class.java).fromJson(src)
