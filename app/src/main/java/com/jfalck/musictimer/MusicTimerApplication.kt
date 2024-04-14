package com.jfalck.musictimer

import android.app.Application
import com.jfalck.musictimer.di.KoinModules.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MusicTimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MusicTimerApplication)
            modules(appModule)
        }
    }
}
