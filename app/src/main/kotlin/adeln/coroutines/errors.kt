package adeln.coroutines

import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

data class Error(val status: Int, val exception: String, val body: Any?)
data class ClientError(val error: Error) : Exception()
data class ServerError(val code: Int, val body: String) : Exception()

fun <T> parseError(resp: Response<T>, moshi: Moshi): Exception =
        when {
            resp.code() < 500 -> parseClient(moshi, resp)
            else              -> parseServer(resp)
        }

fun <T> parseClient(moshi: Moshi, resp: Response<T>): Exception =
        try {
            ClientError(moshi.fromJson(resp.errorBody().source()))
        } catch (e: Exception) {
            parseServer(resp)
        }

fun <T> parseServer(resp: Response<T>): ServerError =
        ServerError(resp.code(), resp.errorBody().string())

class ParseErrorCall<T>(val wrapped: Call<T>, val moshi: Moshi) : Call<T> by wrapped {
    override fun enqueue(callback: Callback<T>): Unit =
            wrapped.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>?, t: Throwable?): Unit =
                        callback.onFailure(call, t)

                override fun onResponse(call: Call<T>?, resp: Response<T>): Unit =
                        when {
                            resp.isSuccessful -> callback.onResponse(call, resp)
                            else              -> callback.onFailure(call, parseError(resp, moshi))
                        }
            })

    override fun execute(): Response<T> =
            wrapped.execute().let { resp ->
                when {
                    resp.isSuccessful -> resp
                    else              -> throw parseError(resp, moshi)
                }
            }
}

class ErrorAdapter<T>(val type: Type, val moshi: Moshi) : CallAdapter<T, Call<T>> {
    override fun responseType(): Type =
            type

    override fun adapt(call: Call<T>): Call<T> =
            ParseErrorCall(call, moshi)

}

class ErrorFactory(val moshi: Moshi) : CallAdapter.Factory() {
    override fun get(returnType: Type,
                     annotations: Array<out Annotation>?,
                     retrofit: Retrofit?): CallAdapter<*, *> =
            ErrorAdapter<Any>(getParameterUpperBound(0, returnType as ParameterizedType?), moshi)
}
