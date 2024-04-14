package com.jfalck.killtimer

import android.app.Application
import com.jfalck.killtimer.di.KoinModules.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KillTimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KillTimerApplication)
            modules(appModule)
        }
    }
}
