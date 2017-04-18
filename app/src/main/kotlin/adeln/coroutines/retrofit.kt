package adeln.coroutines

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

suspend fun <T> Call<T>.await(): T =
        suspendCancellableCoroutine { cont ->

            cont.invokeOnCompletion {
                if (cont.isCancelled) {
                    cancel()
                }
            }

            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>?, t: Throwable) =
                        cont.resumeWithException(t)

                override fun onResponse(call: Call<T>?, resp: Response<T>): Unit =
                        cont.resume(resp.body())
            })
        }
