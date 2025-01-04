package com.jfalck.musictimer.presenter.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jfalck.musictimer.AdManager
import com.jfalck.musictimer.presenter.TextManager
import com.jfalck.musictimer.presenter.TileManager
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.ui.screen.main.MainScreen
import com.jfalck.musictimer.presenter.ui.screen.settings.SettingsScreen
import com.jfalck.musictimer.presenter.vibration.VibratorManager
import com.jfalck.musictimer.presenter.viewmodel.AdsViewModel
import com.jfalck.musictimer.presenter.viewmodel.BillingViewModel
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModel()
    private val adsViewModel: AdsViewModel by viewModel()
    private val billingViewModel: BillingViewModel by viewModel()

    // TODO : Move all of these in viewModels
    private val notificationManager: TimerNotificationManager by inject()
    private val vibratorManager: VibratorManager by inject()
    private val textManager: TextManager by inject()
    private val adManager: AdManager by inject()
    private val tileManager: TileManager by inject()

    private var isServiceBound: Boolean = false

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service $name connected")
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service $name disconnected")
            isServiceBound = false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // do nothing
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        initView()
        billingViewModel.initBilling()
        lifecycleScope.launch {
            Log.d(TAG, "lifecycleScope.launch")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle")
                async { initLoadInterstitialAdListeners() }
                async { initShowInterstitialAdListeners() }
                async { observeBillingConnection() }
                async { observeBillingPurchases() }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    async { initTileSuggestionListener() }
                }
            }
        }
    }

    private suspend fun observeBillingPurchases() {
        billingViewModel.purchases.collect { purchases ->
            Log.d(TAG, purchases.toString())
        }
    }

    private suspend fun observeBillingConnection() {
        billingViewModel.isBillingSystemConnected.collect { isBillingSystemConnected ->
            if (isBillingSystemConnected) {
                billingViewModel.getPurchases()
            }
        }
    }

    private suspend fun initLoadInterstitialAdListeners() {
        adsViewModel.shouldLoadInterstitialAd.collect { shouldLoadInterstitialAd ->
            Log.d(TAG, "shouldLoadInterstitialAd: $shouldLoadInterstitialAd")
            if (shouldLoadInterstitialAd) {
                adManager.loadInterstititalAd({ adsViewModel.isInterstitialAdLoaded = true })
            }
        }
    }

    private suspend fun initShowInterstitialAdListeners() {
        adsViewModel.shouldShowInterstitialAd.collect { shouldShowInterstitialAd ->
            Log.d(TAG, "shouldShowInterstitialAd: $shouldShowInterstitialAd")
            adManager.showIntesistialAd(this)
            adsViewModel.isInterstitialAdLoaded = false
        }
    }

    @SuppressLint("NewApi")
    private suspend fun initTileSuggestionListener() {
        Log.d(TAG, "initTileSuggestionListener()")
        timerViewModel.showTileAdditionSuggestion.collect { shouldDisplayTileAddSuggestion ->
            Log.d(TAG, "shouldDisplayTileAddSuggestion: $shouldDisplayTileAddSuggestion")
            if (shouldDisplayTileAddSuggestion) {
                tileManager.suggestTile()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() =
        requestPermissionLauncher.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
            ?.launch(Manifest.permission.POST_NOTIFICATIONS)

    private fun initView() {
        setContent {
            notificationManager.SetPrimaryColor()
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = MainScreen) {
                composable<MainScreen> {
                    MainScreen(timerViewModel = timerViewModel,
                        adsViewModel = adsViewModel,
                        navController = navController,
                        vibratorManager = vibratorManager,
                        textManager = textManager,
                        startMuteTimer = { position: Float ->
                            timerViewModel.onStartTimer(
                                this@MainActivity, connection, position.toInt()
                            )
                            adsViewModel.updateAdState()
                        },
                        stopMuteTimer = {
                            timerViewModel.stopMuteTimer(this@MainActivity)
                        })
                }
                composable<SettingsScreen> {
                    SettingsScreen(
                        timerViewModel = timerViewModel,
                        adsViewModel = adsViewModel,
                        navController = navController,
                        onRemoveAdsClick = { billingViewModel.showBillingDialog(this@MainActivity) },
                        vibratorManager = vibratorManager,
                        textManager = textManager
                    )
                }
            }
        }
    }
}