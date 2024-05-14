package com.jfalck.musictimer_common.di

import com.jfalck.musictimer_common.data.DataStoreManager
import com.jfalck.musictimer_common.wear.MusicTimerWearableMessageSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

object CommonKoinModules {

    // Dispatchers
    const val IO_DISPATCHER_NAME = "IoDispatcher"
    const val DEFAULT_DISPATCHER_NAME = "DefaultDispatcher"
    const val MAIN_DISPATCHER_NAME = "MainDispatcher"

    val commonModule = module {
        single<DataStoreManager> { DataStoreManager(androidContext()) }
        single { MusicTimerWearableMessageSender(androidContext()) }

        single<CoroutineDispatcher>(named(IO_DISPATCHER_NAME)) { Dispatchers.IO }
        single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER_NAME)) { Dispatchers.Default }
        single<CoroutineDispatcher>(named(MAIN_DISPATCHER_NAME)) { Dispatchers.Main }
    }
}