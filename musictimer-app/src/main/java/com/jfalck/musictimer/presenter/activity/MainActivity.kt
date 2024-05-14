package com.jfalck.musictimer.presenter.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jfalck.musictimer.BuildConfig
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.service.mute.MuteService
import com.jfalck.musictimer.presenter.ui.AdmobBanner
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModel()

    private val notificationManager: TimerNotificationManager by inject()

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


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        initMuteService()
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
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() =
        requestPermissionLauncher.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
            ?.launch(Manifest.permission.POST_NOTIFICATIONS)

    private fun initMuteService() {
        Log.d("MainActivity", "Instantiating MuteService")
        Intent(this, MuteService::class.java).apply {
            bindService(this, connection, Context.BIND_AUTO_CREATE)
            startService(this)
        }
    }

    private fun onTimerButtonClick(sliderPosition: Float, timerRunning: Boolean) {
        if (timerRunning) {
            timerViewModel.stopMuteTimer()
        } else {
            timerViewModel.startTimer(sliderPosition)
            Toast.makeText(
                this,
                getString(R.string.timer_start_toast, sliderPosition.toInt()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initView() {
        installSplashScreen()
        val onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
        setContent {
            notificationManager.SetPrimaryColor()
            val timerRunning by timerViewModel.isTimerRunning.collectAsState(initial = false)
            val initialSliderPosition by
            timerViewModel.timeValueSelected.collectAsState(initial = 0f).asFloatState()

            MainActivityContent(
                timerRunning = timerRunning,
                topAppBarTitle = getString(R.string.app_name),
                onSettingsClick = onSettingsClick,
                initialSliderPosition = initialSliderPosition,
                onSliderValueChanged = { sliderValue ->
                    timerViewModel.setTimeValueSelected(
                        sliderValue
                    )
                },
                onTimerButtonClick = ::onTimerButtonClick,
                buttonText = getString(if (timerRunning) R.string.stop_timer else R.string.start_timer)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    timerRunning: Boolean,
    topAppBarTitle: String,
    onSettingsClick: () -> Unit = {},
    initialSliderPosition: Float,
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
                        showSettingsButton = BuildConfig.DEBUG,
                        onSettingsClick = onSettingsClick,
                        scrollBehavior = scrollBehavior
                    )
                },
            ) { innerPadding ->
                MainActivitySubContent(
                    innerPadding = innerPadding,
                    sliderPosition = initialSliderPosition,
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
    innerPadding: PaddingValues,
    sliderPosition: Float,
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
            steps = 91,
            enabled = true
        )

        Text(
            text = "${sliderPosition.toInt()} minutes",
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
        AdmobBanner(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityPreview() {
    MainActivityContent(
        timerRunning = false,
        topAppBarTitle = "MusicTimer",
        initialSliderPosition = 0.2f,
        onSliderValueChanged = { },
        onTimerButtonClick = { _, _ -> },
        buttonText = "Start timer"
    )
}