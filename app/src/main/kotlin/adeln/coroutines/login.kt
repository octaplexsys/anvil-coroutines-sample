package adeln.coroutines

import android.widget.LinearLayout
import com.squareup.moshi.Moshi
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

sealed class RemoteData<out T, out E>
object NotCalled : RemoteData<Nothing, Nothing>()
object Loading : RemoteData<Nothing, Nothing>()
data class Success<out T>(val t: T) : RemoteData<T, Nothing>()
data class Failure<out E>(val e: E) : RemoteData<Nothing, E>()

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

    var remote: RemoteData<Login, Error> by RenderProp(NotCalled)
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
                    val errors = validate(state)

                    state.loginError = loginError(errors)
                    state.passwordError = passwordError(errors)

                    if (errors.isEmpty()) {
                        state.remote = Loading

                        val client = mkClient()
                        val moshi = Moshi.Builder().build()

                        val creds = Credentials(state.login.toString(),
                                                state.password.toString())

                        async(UI) {
                            try {
                                state.remote = client.newCall(loginRequest(moshi, creds)).await()
                                        .use { r ->
                                            Success(moshi.fromJson<Login>(r.body().source()))
                                        }
                            } catch (e: Exception) {
                                if (e is HttpException) {
                                    state.remote = e.resp.use { Failure(moshi.fromJson(it.body().source())) }
                                } else Unit
                            }
                        }
                    }
                }
            }
        }
