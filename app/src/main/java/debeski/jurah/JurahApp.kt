package debeski.jurah

import android.app.Application
import debeski.jurah.alarm.DoseNotifications
import debeski.jurah.di.AppContainer

class JurahApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        DoseNotifications.ensureChannel(this)
    }
}
