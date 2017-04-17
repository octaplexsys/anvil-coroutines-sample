package adeln.coroutines

import android.app.Activity
import android.content.Context
import android.os.Bundle

val Context.ctx: Context
    get() = this

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = LoginState()

        setContentView(
                renderableView {
                    loginView(state)
                }
        )
    }
}
