package adeln.coroutines

sealed class RemoteData<out T>
object NotCalled : RemoteData<Nothing>()
object Loading : RemoteData<Nothing>()
data class Success<out T>(val t: T) : RemoteData<T>()
data class Failure(val e: Exception) : RemoteData<Nothing>()

inline fun <T> tryRemote(f: () -> T): RemoteData<T> =
    try {
        Success(f())
    } catch (e: Exception) {
        Failure(e)
    }
