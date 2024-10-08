package com.jfalck.musictimer.watch.di

import com.jfalck.musictimer.watch.presentation.viewmodel.TimerRunningViewModel
import com.jfalck.musictimer.watch.presentation.wear.WearableMessageManager
import com.jfalck.musictimer.watch.data.repository.TimerRunningRepository
import com.jfalck.musictimer.watch.common.wear.WatchWearMessageProcessor
import com.jfalck.musictimer_common.common.wear.IWearMessageProcessor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object KoinModules {

    val appModule = module {

        single { TimerRunningRepository() }

        viewModel { TimerRunningViewModel(get()) }

        single { WearableMessageManager(get()) }
        single<IWearMessageProcessor> { WatchWearMessageProcessor(get()) }

    }
}