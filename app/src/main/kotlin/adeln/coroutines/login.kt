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
import trikita.anvil.design.DesignDSL.textInputLayout

sealed class RemoteData<out T>
object NotCalled : RemoteData<Nothing>()
object Loading : RemoteData<Nothing>()
data class Success<out T>(val t: T) : RemoteData<T>()
data class Failure(val e: Exception) : RemoteData<Nothing>()

enum class LoginError {
    EMPTY_LOGIN,
    EMPTY_PASSWORD,
    WRONG_CREDENTIALS,
}

class LoginState {
    var login: CharSequence = ""
    var loginError: CharSequence? = null

    var password: CharSequence = ""
    var passwordError: CharSequence? = null

    var remote: RemoteData<Auth> by RenderProp(NotCalled)
}

fun validate(s: LoginState): Set<LoginError> =
        mutableSetOf<LoginError>().also {

            if (s.login.isEmpty()) {
                it += LoginError.EMPTY_LOGIN
            }

            if (s.password.isEmpty()) {
                it += LoginError.EMPTY_PASSWORD
            }

            val remote = s.remote
            if (remote is Failure
                    && remote.e is ClientError
                    && remote.e.error.status == 401) {

                it += LoginError.WRONG_CREDENTIALS
            }
        }

fun loginError(errors: Set<LoginError>): String? =
        when {
            LoginError.EMPTY_LOGIN in errors       -> "Введи логин"
            LoginError.WRONG_CREDENTIALS in errors -> "Неверный логин или пароль"
            else                                   -> null
        }

fun passwordError(errors: Set<LoginError>): String? =
        when {
            LoginError.EMPTY_PASSWORD in errors    -> "Введи пароль"
            LoginError.WRONG_CREDENTIALS in errors -> "Неверный логин или пароль"
            else                                   -> null
        }

val MAPISSUES = mkMapissues(mkClient(), mkMoshi())

fun loginView(state: LoginState): Void? =
        linearLayout {
            size(MATCH, MATCH)
            orientation(LinearLayout.VERTICAL)

            val notLoading = state.remote !is Loading

            textInputLayout {
                size(MATCH, WRAP)
                error(state.loginError)

                textInputEditHack(123) {
                    size(MATCH, WRAP)
                    enabled(notLoading)

                    onTextChanged {
                        state.login = it
                        state.remote = NotCalled
                    }
                }
            }

            textInputLayout {
                size(MATCH, WRAP)
                error(state.passwordError)

                textInputEditHack(1234) {
                    size(MATCH, WRAP)
                    enabled(notLoading)

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
    val errors = validate(state)

    state.loginError = loginError(errors)
    state.passwordError = passwordError(errors)

    if (errors.isEmpty()) {
        state.remote = Loading

        val creds = Credentials(
                login = state.login.toString(),
                password = state.password.toString()
        )

        async(UI) {
            state.remote = try {
                Success(MAPISSUES.auth(creds).await())
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }
}
