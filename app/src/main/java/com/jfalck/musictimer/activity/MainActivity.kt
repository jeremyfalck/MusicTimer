package com.jfalck.musictimer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.data.DataStoreManager
import com.jfalck.musictimer.di.KoinModules.IO_DISPATCHER_NAME
import com.jfalck.musictimer.notification.TimerNotificationManager.Companion.ACTION_STOP
import com.jfalck.musictimer.service.MuteBinder
import com.jfalck.musictimer.service.MuteService
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

    private var service: MuteBinder? = null
    private var isServiceBound: Boolean = false

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service $name connected")
            service = binder as? MuteBinder
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

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Local intent received with action: ${intent.action}")
            if (intent.action == ACTION_STOP) {
                service?.stopMuteTimer()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        initMuteService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                localBroadcastReceiver, IntentFilter(ACTION_STOP), RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(localBroadcastReceiver, IntentFilter(ACTION_STOP))
        }
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(localBroadcastReceiver)
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
        service?.startMuteTimer(time) ?: Log.d(TAG, "Service not bound")
        Toast.makeText(this, "Timer started for $time minutes", Toast.LENGTH_SHORT).show()
    }

    private fun initView() =
        setContent {
            var sliderPosition by remember { mutableFloatStateOf(initialSliderPosition) }
            MusicTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
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
                        )


                        Button(
                            onClick = { startTimer(sliderPosition.toInt()) },
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text("Start Timer")
                        }
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
                horizontalAlignment = Alignment.CenterHorizontally
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
            }
        }
    }
}