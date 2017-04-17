package adeln.coroutines

import android.widget.LinearLayout
import trikita.anvil.BaseDSL.onTextChanged
import trikita.anvil.DSL.*
import trikita.anvil.design.DesignDSL.*
import trikita.anvil.design.DesignDSL.error as designError

sealed class RemoteData<out T>
object NotCalled : RemoteData<Nothing>()
object Loading : RemoteData<Nothing>()
data class Success<out T>(val t: T) : RemoteData<T>()
data class Failure(val e: Throwable) : RemoteData<Nothing>()

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

    var remote: RemoteData<Unit> by RenderProp(NotCalled)
}

fun validate(s: LoginState): Set<LoginError> =
        mutableSetOf<LoginError>().also {

            if (s.login.isEmpty()) {
                it += LoginError.EMPTY_LOGIN
            }

            if (s.password.isEmpty()) {
                it += LoginError.EMPTY_PASSWORD
            }

            if (s.remote is Failure) {
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

fun loginView(state: LoginState): Void? =
        linearLayout {
            size(MATCH, MATCH)
            orientation(LinearLayout.VERTICAL)

            val notLoading = state.remote !is Loading

            textInputLayout {
                textInputEditText {
                    text(state.login)
                    enabled(notLoading)

                    onTextChanged {
                        state.login = it
                    }
                }

                designError(state.loginError)
            }

            textInputLayout {
                textInputEditText {
                    text(state.password)
                    enabled(notLoading)

                    onTextChanged {
                        state.password = it
                    }
                }

                designError(state.passwordError)
            }

            button {
                enabled(notLoading)

                onClick {
                    val errors = validate(state)

                    state.loginError = loginError(errors)
                    state.passwordError = passwordError(errors)

                    if (errors.isEmpty()) {
                        state.remote = Loading
                    }
                }
            }
        }
