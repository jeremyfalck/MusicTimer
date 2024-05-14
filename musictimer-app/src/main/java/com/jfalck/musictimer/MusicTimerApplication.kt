package com.jfalck.musictimer

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.jfalck.musictimer.di.KoinModules.appModule
import com.jfalck.musictimer_common.di.CommonKoinModules.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class MusicTimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initAds()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@MusicTimerApplication)
            modules(appModule, commonModule)
        }
    }

    private fun initAds() =
        MobileAds.initialize(this)
}
