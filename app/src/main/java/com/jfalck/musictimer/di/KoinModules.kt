package com.jfalck.musictimer.di

import com.jfalck.musictimer.data.DataStoreManager
import com.jfalck.musictimer.manager.MuteManager
import com.jfalck.musictimer.notification.TimerNotificationManager
import com.jfalck.musictimer.service.MuteBinder
import com.jfalck.musictimer.wear.WearableMessageSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinModules {

    // Dispatchers
    const val IO_DISPATCHER_NAME = "IoDispatcher"
    const val DEFAULT_DISPATCHER_NAME = "DefaultDispatcher"
    const val MAIN_DISPATCHER_NAME = "MainDispatcher"

    val appModule = module {
        single<MuteManager> { MuteManager(androidContext()) }
        single<DataStoreManager> { DataStoreManager(androidContext()) }
        single<TimerNotificationManager> {
            TimerNotificationManager(androidContext(), get(), get(named(IO_DISPATCHER_NAME)))
        }
        single<WearableMessageSender> { WearableMessageSender(androidContext()) }

        single<MuteBinder> { MuteBinder(get(), get(), get(), get(named(IO_DISPATCHER_NAME)), get()) }

        single<CoroutineDispatcher>(named(IO_DISPATCHER_NAME)) { Dispatchers.IO }
        single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER_NAME)) { Dispatchers.Default }
        single<CoroutineDispatcher>(named(MAIN_DISPATCHER_NAME)) { Dispatchers.Main }
    }
}