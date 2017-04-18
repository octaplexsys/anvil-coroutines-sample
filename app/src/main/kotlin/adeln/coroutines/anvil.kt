package adeln.coroutines

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.widget.LinearLayout
import trikita.anvil.Anvil
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.init
import trikita.anvil.DSL.withId
import trikita.anvil.RenderableView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RenderProp<T>(var t: T) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            t

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        t = value
        Anvil.render()
    }
}

val Context.ctx: Context
    get() = this

inline fun Context.renderableView(crossinline f: () -> Unit): RenderableView =
        object : RenderableView(ctx) {
            override fun view(): Unit =
                    f()
        }

typealias IdRes = Int

inline fun textInputEditHack(id: IdRes, crossinline f: () -> Unit) {
    init {
        val v = Anvil.currentView<TextInputLayout>()
        v.addView(TextInputEditText(v.context).also { it.id = id },
                  v.childCount,
                  LinearLayout.LayoutParams(MATCH, WRAP))
    }

    withId(id) {
        f()
    }
}
