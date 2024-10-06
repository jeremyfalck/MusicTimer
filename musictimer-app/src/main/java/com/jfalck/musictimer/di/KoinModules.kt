package com.jfalck.musictimer.di

import com.jfalck.musictimer.common.media.MediaFocusManager
import com.jfalck.musictimer.common.wear.PhoneWearMessageProcessor
import com.jfalck.musictimer.data.ITimeValueRepository
import com.jfalck.musictimer.data.TimeValueRepository
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import com.jfalck.musictimer.presenter.wear.WearableMessageManager
import com.jfalck.musictimer.usecase.GetLastTimeValueSelectedUseCase
import com.jfalck.musictimer.usecase.SetLastTimeValueSelectedUseCase
import com.jfalck.musictimer_common.common.wear.IWearMessageProcessor
import com.jfalck.musictimer_common.di.CommonKoinModules.IO_DISPATCHER_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

object KoinModules {

    val appModule = module {

        single<MediaFocusManager> { MediaFocusManager(androidContext()) }
        single<TimerNotificationManager> {
            TimerNotificationManager(androidContext(), get(), get(named(IO_DISPATCHER_NAME)))
        }
        single<WearableMessageManager> { WearableMessageManager(get()) }

        single<IWearMessageProcessor> { PhoneWearMessageProcessor(androidContext(), get()) }

        single<MuteBinder> {
            MuteBinder(get(), get(), get(), get(named(IO_DISPATCHER_NAME)), get())
        }

        single<ITimeValueRepository> { TimeValueRepository(get()) }

        single<GetLastTimeValueSelectedUseCase> { GetLastTimeValueSelectedUseCase(get()) }
        single<SetLastTimeValueSelectedUseCase> { SetLastTimeValueSelectedUseCase(get()) }

        single { MuteServiceManager() }

        viewModel {
            TimerViewModel(get(), get(named(IO_DISPATCHER_NAME)), get(), get())
        }
    }
}