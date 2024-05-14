package com.jfalck.musictimer.watch.di

import com.jfalck.musictimer.watch.presentation.viewmodel.TimerRunningViewModel
import com.jfalck.musictimer.watch.presentation.wear.WearableMessageManager
import com.jfalck.musictimer.watch.repository.TimerRunningRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object KoinModules {

    val appModule = module {

        single { TimerRunningRepository() }

        single { WearableMessageManager(get()) }

        viewModel { TimerRunningViewModel(get()) }

    }
}