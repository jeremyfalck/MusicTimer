package com.jfalck.musictimer.di

import com.jfalck.musictimer.AdManager
import com.jfalck.musictimer.billing.BillingManager
import com.jfalck.musictimer.common.media.MediaFocusManager
import com.jfalck.musictimer.common.wear.PhoneWearMessageProcessor
import com.jfalck.musictimer.data.ITimeValueRepository
import com.jfalck.musictimer.data.TimeValueRepository
import com.jfalck.musictimer.data.datasource.FirestoreDataSource
import com.jfalck.musictimer.data.mapper.PurchaseMapper
import com.jfalck.musictimer.presenter.TextManager
import com.jfalck.musictimer.presenter.TileManager
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer.presenter.vibration.VibratorManager
import com.jfalck.musictimer.presenter.viewmodel.AdsViewModel
import com.jfalck.musictimer.presenter.viewmodel.BillingViewModel
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import com.jfalck.musictimer.presenter.wear.WearableMessageManager
import com.jfalck.musictimer.usecase.GetLastTimeValueSelectedUseCase
import com.jfalck.musictimer.usecase.GetQuickSettingsTimeValueUseCase
import com.jfalck.musictimer.usecase.GetTileAdditionSuggestionUseCase
import com.jfalck.musictimer.usecase.IncrementLaunchCountUseCase
import com.jfalck.musictimer.usecase.IsPaidUserUseCase
import com.jfalck.musictimer.usecase.SavePurchasesUseCase
import com.jfalck.musictimer.usecase.SetIsPaidUserUseCase
import com.jfalck.musictimer.usecase.SetLastTimeValueSelectedUseCase
import com.jfalck.musictimer.usecase.SetQuickSettingsTimeValueUseCase
import com.jfalck.musictimer.usecase.ShouldLoadInterstitialAdUseCase
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
        single { MuteServiceManager(get()) }
        single { VibratorManager(androidContext()) }
        single { TextManager(androidContext()) }
        single { BillingManager(androidContext()) }
        single { AdManager(androidContext()) }
        single { TileManager(androidContext()) }

        single<ITimeValueRepository> { TimeValueRepository(get()) }

        single<PurchaseMapper> { PurchaseMapper() }
        single<FirestoreDataSource> { FirestoreDataSource() }

        // Region Use Case
        single<GetLastTimeValueSelectedUseCase> { GetLastTimeValueSelectedUseCase(get()) }
        single<SetLastTimeValueSelectedUseCase> { SetLastTimeValueSelectedUseCase(get()) }
        single<GetTileAdditionSuggestionUseCase> { GetTileAdditionSuggestionUseCase(get()) }
        single<IncrementLaunchCountUseCase> { IncrementLaunchCountUseCase(get()) }
        single<ShouldLoadInterstitialAdUseCase> { ShouldLoadInterstitialAdUseCase(get()) }
        single<IsPaidUserUseCase> { IsPaidUserUseCase(get()) }
        single<GetQuickSettingsTimeValueUseCase> { GetQuickSettingsTimeValueUseCase(get()) }
        single<SetQuickSettingsTimeValueUseCase> { SetQuickSettingsTimeValueUseCase(get()) }
        single<SetIsPaidUserUseCase> { SetIsPaidUserUseCase(get()) }
        single<SavePurchasesUseCase> { SavePurchasesUseCase(get(), get()) }
        // End Region

        //Region View Model

        viewModel<TimerViewModel> {
            TimerViewModel(get(), get(), get(), get(), get(), get(), get())
        }
        viewModel<AdsViewModel> {
            AdsViewModel(get(), get())
        }
        viewModel<BillingViewModel> {
            BillingViewModel(get(), get(), get())
        }

        // End Region
    }
}