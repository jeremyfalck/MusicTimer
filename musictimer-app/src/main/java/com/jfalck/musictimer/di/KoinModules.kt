package com.jfalck.musictimer.di

import com.jfalck.musictimer.common.media.MediaFocusManager
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.common.wear.PhoneWearMessageProcessor
import com.jfalck.musictimer.presenter.wear.WearableMessageManager
import com.jfalck.musictimer_common.di.CommonKoinModules.IO_DISPATCHER_NAME
import com.jfalck.musictimer_common.common.wear.IWearMessageProcessor
import org.koin.android.ext.koin.androidContext
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
    }
}