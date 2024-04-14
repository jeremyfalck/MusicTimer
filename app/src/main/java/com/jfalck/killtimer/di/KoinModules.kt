package com.jfalck.killtimer.di

import com.jfalck.killtimer.MuteBinder
import com.jfalck.killtimer.notification.TimerNotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object KoinModules {

    val appModule = module {
        single<TimerNotificationManager> { TimerNotificationManager(androidContext()) }
        single<MuteBinder> { MuteBinder(get()) }
    }

}