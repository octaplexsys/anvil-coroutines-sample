package adeln.coroutines

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import timber.log.Timber

data class Credentials(
        val login: String,
        val password: String
)

data class User(
        val id: Long,
        val email: String,
        val firstName: String,
        val lastName: String,
        val patronymic: String
)

data class Auth(
        val token: String,
        val ttl: Long,
        val refreshToken: String,
        val user: User,
        val expiresIn: Long,
        val tokenType: String,
        val accessToken: String
)

interface Mapissues {

    @POST("auth")
    fun auth(@Body c: Credentials): Call<Auth>
}

fun mkLogger(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { Timber.d(it) }
                .setLevel(HttpLoggingInterceptor.Level.BODY)

fun mkClient(): OkHttpClient =
        OkHttpClient.Builder()
                .addInterceptor(mkLogger())
                .build()

fun mkMoshi(): Moshi =
        Moshi.Builder().build()

fun mkMapissues(client: OkHttpClient, moshi: Moshi): Mapissues =
        Retrofit.Builder()
                .client(client)
                .baseUrl("http://mapissues-master.infotech.team/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(ErrorFactory(moshi))
                .build()
                .create(Mapissues::class.java)
