package com.jfalck.musictimer.presenter.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jfalck.musictimer.BuildConfig
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.tile.TimerTileService
import com.jfalck.musictimer.presenter.ui.AdmobBanner
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer.presenter.viewmodel.AdsViewModel
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
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

    private fun initView() {
        installSplashScreen()
        val onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
        val vibrator = getSystemService(Vibrator::class.java)
        setContent {
            notificationManager.SetPrimaryColor()
            val timerRunning by timerViewModel.isTimerRunning.collectAsState(initial = false)
            val sliderPosition by
            timerViewModel.timeValueSelected.collectAsState(initial = 1f).asFloatState()
            val isPaidUser: Boolean by adsViewModel.isPaidUser.collectAsState()

            val intValue = sliderPosition.toInt()

            val sliderText = resources.getQuantityString(
                R.plurals.timer_value_selected,
                intValue,
                intValue
            )

            val snackbarHostState = remember { SnackbarHostState() }
            val snackBarCoroutineScope = rememberCoroutineScope()

            MainActivityContent(
                snackbarHostState = snackbarHostState,
                isPaidUser = isPaidUser,
                timerRunning = timerRunning,
                topAppBarTitle = getString(R.string.app_name),
                onSettingsClick = onSettingsClick,
                sliderPosition = sliderPosition,
                sliderText = sliderText,
                onSliderValueChanged = { sliderValue ->
                    timerViewModel.setTimeValueSelected(
                        sliderValue
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                },
                onTimerButtonClick = { position: Float, isTimerRunning: Boolean ->
                    onTimerButtonClick(position, isTimerRunning)
                    if (!isTimerRunning) {
                        snackBarCoroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = getString(
                                    R.string.timer_start_toast,
                                    sliderPosition.toInt()
                                ),
                                actionLabel = "OK"
                            )
                        }
                    }
                },
                buttonText = getString(if (timerRunning) R.string.stop_timer else R.string.start_timer)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    snackbarHostState: SnackbarHostState,
    isPaidUser: Boolean,
    timerRunning: Boolean,
    topAppBarTitle: String,
    onSettingsClick: () -> Unit = {},
    sliderPosition: Float,
    sliderText: String,
    onSliderValueChanged: (Float) -> Unit,
    onTimerButtonClick: (Float, Boolean) -> Unit,
    buttonText: String
) {
    MusicTimerTheme(
        darkTheme = isSystemInDarkTheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            val scrollBehavior =
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                topBar = {
                    CenterAlignedTopAppBar(
                        title = topAppBarTitle,
                        showSettingsButton = true,
                        onSettingsClick = onSettingsClick,
                        scrollBehavior = scrollBehavior
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
            ) { innerPadding ->
                MainActivitySubContent(
                    isPaidUser = isPaidUser,
                    innerPadding = innerPadding,
                    sliderPosition = sliderPosition,
                    sliderText = sliderText,
                    onSliderValueChanged = onSliderValueChanged,
                    timerRunning = timerRunning,
                    onTimerButtonClick = onTimerButtonClick,
                    buttonText = buttonText
                )
            }

        }
    }
}

@Composable
fun MainActivitySubContent(
    isPaidUser: Boolean,
    innerPadding: PaddingValues,
    sliderPosition: Float,
    sliderText: String,
    onSliderValueChanged: (Float) -> Unit,
    timerRunning: Boolean,
    onTimerButtonClick: (Float, Boolean) -> Unit,
    buttonText: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Slider(
            modifier = Modifier.padding(16.dp),
            value = sliderPosition,
            valueRange = 1F..90F,
            onValueChange = onSliderValueChanged,
            steps = 90,
            enabled = true
        )

        Text(
            text = sliderText,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.secondary
        )


        Button(
            onClick = { onTimerButtonClick(sliderPosition, timerRunning) },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(buttonText)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (!isPaidUser) {
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityPreview() {
    MainActivityContent(
        SnackbarHostState(),
        true,
        timerRunning = false,
        topAppBarTitle = "MusicTimer",
        sliderPosition = 30f,
        sliderText = "30 minutes",
        onSliderValueChanged = { },
        onTimerButtonClick = { _, _ -> },
        buttonText = "Start timer"
    )
}