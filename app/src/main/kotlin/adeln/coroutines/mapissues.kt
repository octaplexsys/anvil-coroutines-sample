package adeln.coroutines

import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

data class Credentials(
        val login: String,
        val password: String
)

data class Error(
        val status: Int,
        val exception: String,
        val body: Any?
)

data class User(
        val id: Long,
        val email: String,
        val firstName: String,
        val lastName: String,
        val patronymic: String
)

data class Login(
        val token: String,
        val ttl: Long,
        val refreshToken: String,
        val user: User,
        val expiresIn: Long,
        val tokenType: String,
        val accessToken: String
)

fun loginRequest(moshi: Moshi, credentials: Credentials): Request =
        Request.Builder()
                .url("http://mapissues-master.infotech.team/auth")
                .post(RequestBody.create(MediaType.parse("application/json"),
                                         moshi.toJson(credentials)))
                .build()

fun mkClient(): OkHttpClient =
        OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor { Timber.d(it) }
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
