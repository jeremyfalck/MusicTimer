package com.jfalck.musictimer.activity

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jfalck.musictimer.BuildConfig
import com.jfalck.musictimer.R
import com.jfalck.musictimer_common.data.DataStoreManager
import com.jfalck.musictimer.notification.TimerNotificationManager
import com.jfalck.musictimer.service.MuteBinder
import com.jfalck.musictimer.service.MuteService
import com.jfalck.musictimer.ui.AdmobBanner
import com.jfalck.musictimer.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.ui.theme.MusicTimerTheme
import com.jfalck.musictimer_common.di.CommonKoinModules.IO_DISPATCHER_NAME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import kotlin.math.roundToInt

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named(IO_DISPATCHER_NAME))
    private val service: MuteBinder by inject()
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

    private var initialSliderPosition: Float = 0f


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
        setSliderPosition()
        initView()
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() =
        requestPermissionLauncher.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
            ?.launch(Manifest.permission.POST_NOTIFICATIONS)

    private fun setSliderPosition() =
        CoroutineScope(ioDispatcher).launch {
            initialSliderPosition = dataStoreManager.getLastTimeValueSelected().toFloat()
        }

    private fun initView() {
        installSplashScreen()
        val onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
        setContent {
            notificationManager.SetPrimaryColor()
            val timerRunning by service.isTimerRunning.collectAsState()
            MainActivityContent(
                timerRunning = timerRunning,
                topAppBarTitle = getString(R.string.app_name),
                onSettingsClick = onSettingsClick,
                initialSliderPosition = initialSliderPosition,
                onTimerButtonClick = ::onTimerButtonClick,
                buttonText = getString(if (timerRunning) R.string.stop_timer else R.string.start_timer)
            )
        }
    }

    private fun initMuteService() {
        Log.d("MainActivity", "Instantiating MuteService")
        Intent(this, MuteService::class.java).apply {
            bindService(this, connection, Context.BIND_AUTO_CREATE)
            startService(this)
        }
    }

    private fun startTimer(time: Int) {
        Log.d(TAG, "Starting timer for $time minutes")
        CoroutineScope(ioDispatcher).launch {
            dataStoreManager.setLastTimeValueSelected(time)
        }
        service.startMuteTimer(time)
        Toast.makeText(this, getString(R.string.timer_start_toast, time), Toast.LENGTH_SHORT).show()
    }

    private fun onTimerButtonClick(sliderPosition: Int, timerRunning: Boolean) {
        if (timerRunning) {
            service.stopMuteTimer()
        } else {
            startTimer(sliderPosition)
        }
    }
}

@Composable
fun MainActivitySubContent(
    innerPadding: PaddingValues,
    initialSliderPosition: Float,
    timerRunning: Boolean,
    onTimerButtonClick: (Int, Boolean) -> Unit,
    buttonText: String
) {
    var sliderPosition by remember { mutableFloatStateOf(initialSliderPosition) }

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
            onValueChange = { sliderPosition = it.roundToInt().toFloat() },
            steps = 91,
            enabled = true
        )

        Text(
            text = "${sliderPosition.toInt()} minutes",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.secondary
        )


        Button(
            onClick = { onTimerButtonClick(sliderPosition.toInt(), timerRunning) },
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    timerRunning: Boolean,
    topAppBarTitle: String,
    onSettingsClick: () -> Unit = {},
    initialSliderPosition: Float,
    onTimerButtonClick: (Int, Boolean) -> Unit,
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
                    initialSliderPosition = initialSliderPosition,
                    timerRunning = timerRunning,
                    onTimerButtonClick = onTimerButtonClick,
                    buttonText = buttonText
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityPreview() {
    MainActivityContent(
        timerRunning = false,
        topAppBarTitle = "MusicTimer",
        initialSliderPosition = 0.2f,
        onTimerButtonClick = { _, _ -> },
        buttonText = "Start timer"
    )
}