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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.R
import com.jfalck.musictimer.data.DataStoreManager
import com.jfalck.musictimer.di.KoinModules.IO_DISPATCHER_NAME
import com.jfalck.musictimer.service.MuteBinder
import com.jfalck.musictimer.service.MuteService
import com.jfalck.musictimer.ui.AdmobBanner
import com.jfalck.musictimer.ui.theme.MusicTimerTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named(IO_DISPATCHER_NAME))
    private val service: MuteBinder by inject()

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

    private fun setSliderPosition() =
        CoroutineScope(ioDispatcher).launch {
            initialSliderPosition = dataStoreManager.getLastTimeValueSelected().toFloat()
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

    private fun startTimer(time: Int) {
        Log.d(TAG, "Starting timer for $time minutes")
        CoroutineScope(ioDispatcher).launch {
            dataStoreManager.setLastTimeValueSelected(time)
        }
        service.startMuteTimer(time)
        Toast.makeText(this, getString(R.string.timer_start_toast, time), Toast.LENGTH_SHORT).show()
    }

    private fun initView() =
        setContent {
            var sliderPosition by remember { mutableFloatStateOf(initialSliderPosition) }
            val timerRunning by service.isTimerRunning.collectAsState()
            MusicTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
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
                            onClick = {
                                if (timerRunning) {
                                    service.stopMuteTimer()
                                } else {
                                    startTimer(sliderPosition.toInt())
                                }
                            },
                            modifier = Modifier.padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text(getString(if (timerRunning) R.string.stop_timer else R.string.start_timer))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        AdmobBanner(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityPreview() {
    MusicTimerTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Slider(
                    modifier = Modifier.padding(16.dp),
                    value = 15F,
                    valueRange = 1F..90F,
                    onValueChange = { },
                    steps = 90,
                    enabled = true
                )

                Text(
                    text = "${15F.toInt()} minutes",
                    modifier = Modifier.padding(16.dp)
                )


                Button(
                    onClick = {},
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text("Start Timer")
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), onClick = {}) {
                    Text("Start Timer")
                }
            }
        }
    }
}