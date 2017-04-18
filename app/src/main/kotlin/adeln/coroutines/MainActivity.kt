package adeln.coroutines

import android.app.Activity
import android.os.Bundle

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
