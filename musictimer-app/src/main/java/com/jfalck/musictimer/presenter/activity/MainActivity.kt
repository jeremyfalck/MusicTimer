package com.jfalck.musictimer.presenter.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jfalck.musictimer.BuildConfig
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.tile.TimerTileService
import com.jfalck.musictimer.presenter.ui.screen.main.MainScreen
import com.jfalck.musictimer.presenter.ui.screen.settings.SettingsScreen
import com.jfalck.musictimer.presenter.viewmodel.AdsViewModel
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModel()
    private val adsViewModel: AdsViewModel by viewModel()

    private val notificationManager: TimerNotificationManager by inject()

    private var isServiceBound: Boolean = false

    private var interstitialAd: InterstitialAd? = null

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

    private var showQuickTimeSlider = mutableStateOf(false)

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        initView()
        lifecycleScope.launch {
            Log.d(TAG, "lifecycleScope.launch")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "repeatOnLifecycle")
                async { initLoadInterstitialAdListeners() }
                async { initShowInterstitialAdListeners() }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    async { initTileSuggestionListener() }
                }
            }
        }
    }

    private suspend fun initLoadInterstitialAdListeners() {
        adsViewModel.shouldLoadInterstitialAd.collect { shouldLoadInterstitialAd ->
            Log.d(TAG, "shouldLoadInterstitialAd: $shouldLoadInterstitialAd")
            if (shouldLoadInterstitialAd) {
                loadInterstititalAd()
            }
        }
    }

    private suspend fun initShowInterstitialAdListeners() {
        adsViewModel.shouldShowInterstitialAd.collect { shouldShowInterstitialAd ->
            Log.d(TAG, "shouldShowInterstitialAd: $shouldShowInterstitialAd")
            interstitialAd?.show(this)
            adsViewModel.isInterstitialAdLoaded = false
        }
    }

    @SuppressLint("NewApi")
    private suspend fun initTileSuggestionListener() {
        Log.d(TAG, "initTileSuggestionListener()")
        timerViewModel.showTileAdditionSuggestion.collect { shouldDisplayTileAddSuggestion ->
            Log.d(TAG, "shouldDisplayTileAddSuggestion: $shouldDisplayTileAddSuggestion")
            if (shouldDisplayTileAddSuggestion) {
                suggestTile()
            }
        }
    }

    private fun loadInterstititalAd() {
        InterstitialAd.load(
            this,
            BuildConfig.ADMOB_INTERSTITIAL_BANNER_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    interstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    this@MainActivity.interstitialAd = interstitialAd
                    adsViewModel.isInterstitialAdLoaded = true
                }
            })
    }


    @SuppressLint("NewApi")
    private fun suggestTile() {
        Log.d(TAG, "suggestTile()")
        val statusBarService =
            this.getSystemService(StatusBarManager::class.java)

        val componentName = ComponentName(
            this@MainActivity.applicationContext,
            TimerTileService::class.java.getName()
        )
        statusBarService.requestAddTileService(
            componentName,
            this@MainActivity.getString(R.string.timer_tile_label),
            Icon.createWithResource(
                this@MainActivity,
                R.drawable.timer_tile_icon
            ),
            {},
            {}
        )
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() =
        requestPermissionLauncher.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
            ?.launch(Manifest.permission.POST_NOTIFICATIONS)

    private fun onTimerButtonClick(sliderPosition: Float, timerRunning: Boolean) =
        if (timerRunning) {
            timerViewModel.stopMuteTimer(this)
        } else {
            startMuteService(sliderPosition.toInt())
        }

    private fun startMuteService(timeInMinutes: Int) {
        Log.d("MainActivity", "Instantiating MuteService")
        timerViewModel.onStartTimer(this, connection, timeInMinutes)
        adsViewModel.updateAdState()
    }

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d(TAG, "billingResult: $billingResult")
            Log.d(TAG, "purchases: $purchases")
        }

    private var billingClient: BillingClient? = null

    private fun showBillingDialog() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "billingResult is OK")

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("ad_free_plan")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                    Log.d(TAG, "productList loaded: $productList")
                    val params = QueryProductDetailsParams.newBuilder()
                    params.setProductList(productList)

                    CoroutineScope(Dispatchers.IO).launch {
                        val productDetailResult: ProductDetailsResult? =
                            billingClient?.queryProductDetails(params.build())
                        Log.d(TAG, "products found: $productDetailResult")

                        productDetailResult?.productDetailsList?.firstOrNull()
                            ?.let { productDetails ->
                                val productDetailsParamsList = listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )

                                val billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList)
                                    .build()

                                CoroutineScope(Dispatchers.Main).launch {
                                    billingClient?.launchBillingFlow(
                                        this@MainActivity,
                                        billingFlowParams
                                    )
                                }
                            }
                    }

                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service is disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

    private fun initView() {
        installSplashScreen()
        val vibrator = getSystemService(Vibrator::class.java)
        setContent {
            notificationManager.SetPrimaryColor()
            val timerRunning by timerViewModel.isTimerRunning.collectAsState(initial = false)
            val sliderPosition by
            timerViewModel.timeValueSelected.collectAsState(initial = 1f).asFloatState()
            val isPaidUser = adsViewModel.isPaidUser.collectAsState(initial = false)

            // Settings
            val quickSettingsTimeValue =
                timerViewModel.quickSettingsTimeValueSelected.collectAsState()
            val showDialog by remember { showQuickTimeSlider }

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = MainScreen) {
                composable<MainScreen> {
                    MainScreen(
                        context = this@MainActivity,
                        isTimerRunning = timerRunning,
                        sliderPosition = sliderPosition,
                        isPaidUser = isPaidUser.value,
                        notifyTimeValueChanged = {
                            timerViewModel.setTimeValueSelected(it)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                            }
                        },
                        onTimerButtonClick = ::onTimerButtonClick,
                        onSettingsClicked = { navController.navigate(SettingsScreen) }
                    )
                }
                composable<SettingsScreen> {
                    SettingsScreen(
                        topAppBarTitle = getString(R.string.settings),
                        quickTimeSettingTitle = getString(R.string.quick_time_settings),
                        quickTimeSettingDescription = getString(R.string.quick_time_settings_desc),
                        onQuickTimeClicked = { showQuickTimeSlider.value = !showDialog },
                        quickTimeSettingsValue = quickSettingsTimeValue.value,
                        showTimeQuickSettingDialog = showDialog,
                        onQuickTimeValueSelected = { value ->
                            timerViewModel.setQuickSettingsTimeValue(value.toInt())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                            }
                        },
                        onRemoveAdsClick = { showBillingDialog() }
                    )
                }
            }
        }
    }
}