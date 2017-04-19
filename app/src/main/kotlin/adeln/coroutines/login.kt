package adeln.coroutines

import android.widget.LinearLayout
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import trikita.anvil.DSL.MATCH
import trikita.anvil.DSL.WRAP
import trikita.anvil.DSL.button
import trikita.anvil.DSL.enabled
import trikita.anvil.DSL.linearLayout
import trikita.anvil.DSL.onClick
import trikita.anvil.DSL.onTextChanged
import trikita.anvil.DSL.orientation
import trikita.anvil.DSL.size
import trikita.anvil.DSL.text
import trikita.anvil.design.DesignDSL.error
import trikita.anvil.design.DesignDSL.hint
import trikita.anvil.design.DesignDSL.textInputLayout

class LoginState {
    var login: CharSequence = ""
    var password: CharSequence = ""
    var remote: RemoteData<Auth> by RenderProp(NotCalled)
}

fun isGood(s: LoginState): Boolean =
    s.login.isNotBlank() && s.password.isNotBlank()

fun wrongCredentials(remote: RemoteData<Auth>): Boolean =
    remote is Failure
        && remote.e is ClientError
        && remote.e.error.status == 401

fun loginError(state: LoginState): String? =
    when {
        state.login.isBlank()          -> "Введи логин"
        wrongCredentials(state.remote) -> "Неверный логин или пароль"
        else                           -> null
    }

fun passwordError(state: LoginState): String? =
    when {
        state.password.isBlank()       -> "Введи пароль"
        wrongCredentials(state.remote) -> "Неверный логин или пароль"
        else                           -> null
    }

val MAPISSUES = mkMapissues(mkClient(), mkMoshi())

fun loginView(state: LoginState): Void? =
    linearLayout {
        size(MATCH, MATCH)
        orientation(LinearLayout.VERTICAL)

        val notLoading = state.remote !is Loading

        textInputLayout {
            size(MATCH, WRAP)
            error(loginError(state))
            enabled(notLoading)
            hint("Login")

            textInputEditHack(123) {
                size(MATCH, WRAP)

                onTextChanged {
                    state.login = it
                    state.remote = NotCalled
                }
            }
        }

        textInputLayout {
            size(MATCH, WRAP)
            error(passwordError(state))
            enabled(notLoading)
            hint("Password")

            textInputEditHack(1234) {
                size(MATCH, WRAP)

                onTextChanged {
                    state.password = it
                    state.remote = NotCalled
                }
            }
        }

        button {
            enabled(notLoading)
            text("go")

            onClick {
                onLoginClick(state)
            }
        }
    }

fun onLoginClick(state: LoginState) {
    if (isGood(state)) {
        state.remote = Loading

        val creds = Credentials(
            login = state.login.toString(),
            password = state.password.toString()
        )

        async(UI) {
            state.remote = tryRemote {
                MAPISSUES.auth(creds).await()
            }
        }
    }
}
