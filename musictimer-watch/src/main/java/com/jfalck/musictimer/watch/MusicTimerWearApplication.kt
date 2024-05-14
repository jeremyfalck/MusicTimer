package com.jfalck.musictimer.watch

import android.app.Application
import com.jfalck.musictimer.watch.di.KoinModules.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MusicTimerWearApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@MusicTimerWearApplication)
            modules(appModule)
        }
    }
}

