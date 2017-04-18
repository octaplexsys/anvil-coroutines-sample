package adeln.coroutines

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class HttpException(val resp: Response) : Exception()

suspend fun Call.await(): Response =
        suspendCancellableCoroutine { cont ->

            cont.invokeOnCompletion {
                if (cont.isCancelled) {
                    cancel()
                }
            }

            enqueue(object : Callback {
                override fun onFailure(p0: Call?, e: IOException): Unit =
                        cont.resumeWithException(e)

                override fun onResponse(p0: Call?, r: Response): Unit =
                        when {
                            r.isSuccessful -> cont.resume(r)
                            else           -> cont.resumeWithException(HttpException(r))
                        }
            })
        }
