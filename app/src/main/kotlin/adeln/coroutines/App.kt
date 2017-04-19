package adeln.coroutines

import android.app.Application
import com.facebook.soloader.SoLoader
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        Timber.plant(Timber.DebugTree())
    }
}
