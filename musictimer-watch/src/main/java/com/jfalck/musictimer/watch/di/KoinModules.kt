package com.jfalck.musictimer.watch.di

import androidx.lifecycle.viewmodel.viewModelFactory
import com.jfalck.musictimer.watch.presentation.TimerRunningViewModel
import com.jfalck.musictimer.watch.repository.TimerRunningRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinModules {

    // Dispatchers
    const val IO_DISPATCHER_NAME = "IoDispatcher"
    const val DEFAULT_DISPATCHER_NAME = "DefaultDispatcher"
    const val MAIN_DISPATCHER_NAME = "MainDispatcher"

    val appModule = module {

        single { TimerRunningRepository() }

        viewModel { TimerRunningViewModel(get()) }

        single<CoroutineDispatcher>(named(IO_DISPATCHER_NAME)) { Dispatchers.IO }
        single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER_NAME)) { Dispatchers.Default }
        single<CoroutineDispatcher>(named(MAIN_DISPATCHER_NAME)) { Dispatchers.Main }
    }
}