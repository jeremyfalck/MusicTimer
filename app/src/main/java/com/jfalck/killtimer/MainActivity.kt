package com.jfalck.killtimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.killtimer.ui.theme.KillTimerTheme
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {

    private var isServiceBound: Boolean = false
    private var service: MuteBinder? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("MainActivity", "Service $name connected")
            service = binder as? MuteBinder
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("MainActivity", "Service $name disconnected")
            isServiceBound = false
        }
    }

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
        setContent {
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            KillTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Greeting("Android")

                        Slider(
                            modifier = Modifier.padding(16.dp),
                            value = sliderPosition,
                            valueRange = 0F..90F,
                            onValueChange = { sliderPosition = it.roundToInt().toFloat() },
                            steps = 91,
                            enabled = true
                        )

                        Text(text = "${sliderPosition.toInt()} minutes")


                        Button(onClick = { startTimer(sliderPosition.toInt()) }) {
                            Text("Start Timer")
                        }
                    }
                }
            }
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
        Log.d("MainActivity", "Starting timer for $time minutes")
        val countDownValue = time.toLong() * 60 * 1000
        service?.startMuteTimer(countDownValue, this) ?: Log.d("MainActivity", "Service not bound")
        Toast.makeText(this, "Timer started for $time minutes", Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KillTimerTheme {
        Greeting("Android")
    }
}