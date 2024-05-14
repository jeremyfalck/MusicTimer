package com.jfalck.musictimer.di

import com.jfalck.musictimer.manager.MuteManager
import com.jfalck.musictimer.notification.TimerNotificationManager
import com.jfalck.musictimer.service.MuteBinder
import com.jfalck.musictimer.service.WatchWearMessageProcessor
import com.jfalck.musictimer.wear.WearableMessageManager
import com.jfalck.musictimer_common.di.CommonKoinModules.IO_DISPATCHER_NAME
import com.jfalck.musictimer_common.service.IWearMessageProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinModules {

    val appModule = module {
        single<MuteManager> { MuteManager(androidContext()) }
        single<TimerNotificationManager> {
            TimerNotificationManager(androidContext(), get(), get(named(IO_DISPATCHER_NAME)))
        }
        single<WearableMessageManager> { WearableMessageManager(get()) }

        single<IWearMessageProcessor> { WatchWearMessageProcessor(androidContext(), get()) }

        single<MuteBinder> {
            MuteBinder(get(), get(), get(), get(named(IO_DISPATCHER_NAME)), get())
        }
    }
}