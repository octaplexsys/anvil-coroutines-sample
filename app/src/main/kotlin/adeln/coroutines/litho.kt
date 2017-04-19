package adeln.coroutines

import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.Text

inline fun <T> ComponentContext.column(f: ComponentLayout.ContainerBuilder.() -> T): ComponentLayout.ContainerBuilder =
    Column.create(this).also { it.f() }

fun ComponentLayout.ContainerBuilder.children(vararg cs: Component.Builder<*>): Unit =
    cs.forEach {
        child(it)
    }

inline fun <T> ComponentContext.text(f: Text.Builder.() -> T): Text.Builder =
    Text.create(this).also { it.f() }

inline fun <T> ComponentContext.editText(f: EditText.Builder.() -> T): EditText.Builder =
    EditText.create(this).also { it.f() }

inline fun <T> ComponentContext.button(f: Text.Builder.() -> T): Text.Builder =
    Text.create(this).also { it.f() }
