package adeln.coroutines

import android.widget.LinearLayout
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.onTextChanged
import trikita.anvil.DSL.*
import trikita.anvil.design.DesignDSL.*
import trikita.anvil.design.DesignDSL.error as designError

enum class LoginError {
    EMPTY_LOGIN,
    EMPTY_PASSWORD,
    WRONG_CREDENTIALS,
}

class LoginState {
    var login: CharSequence = ""
    var password: CharSequence = ""
    var wrong by RenderProp(false)
}

fun validate(s: LoginState): Set<LoginError> =
        mutableSetOf<LoginError>().also {
            if (s.login.isEmpty()) {
                it += LoginError.EMPTY_LOGIN
            }

            if (s.password.isEmpty()) {
                it += LoginError.EMPTY_PASSWORD
            }

            if (s.wrong) {
                it += LoginError.WRONG_CREDENTIALS
            }
        }

fun loginError(errors: Set<LoginError>): String? =
        when {
            LoginError.EMPTY_LOGIN in errors       -> "Введи логин"
            LoginError.WRONG_CREDENTIALS in errors -> "Неверный логин или пароль"
            else                                   -> null
        }

fun loginView(state: LoginState): Void? =
        linearLayout {
            size(MATCH, MATCH)
            orientation(LinearLayout.VERTICAL)

            val errors = validate(state)

            textInputLayout {

                textInputEditText {
                    text(state.login)

                    onTextChanged {
                        state.login = it
                    }
                }

                loginError(errors)?.let { designError(it) }
            }

            textInputLayout {
                textInputEditText {
                    text(state.password)

                    onTextChanged {
                        state.password = it
                    }
                }

                loginError(errors)?.let { designError(it) }
            }

            button {
                onClick {
                    Anvil.render()
                }
            }
        }
