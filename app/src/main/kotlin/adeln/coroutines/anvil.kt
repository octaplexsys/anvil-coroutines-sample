package adeln.coroutines

import android.content.Context
import android.util.Log
import trikita.anvil.Anvil
import trikita.anvil.RenderableView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RenderProp<T>(var t: T) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            t

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        t = value
        Log.d("coroutine", "render!")
        Anvil.render()
    }
}

inline fun Context.renderableView(crossinline f: () -> Unit): RenderableView =
        object : RenderableView(ctx) {
            override fun view(): Unit =
                    f()
        }
