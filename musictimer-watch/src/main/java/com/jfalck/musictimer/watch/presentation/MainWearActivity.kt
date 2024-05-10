package com.jfalck.musictimer.watch.presentation

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.google.android.gms.wearable.Wearable
import com.jfalck.musictimer.watch.R
import com.jfalck.musictimer.watch.presentation.theme.MusicTimerTheme
import java.nio.charset.Charset
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "MainWearActivity"

class MainWearActivity : ComponentActivity() {

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private var initialSliderPosition: MutableFloatState = mutableFloatStateOf(0f)

    private var timeSelection: MutableIntState = mutableIntStateOf(0)

    private val launchTimer: (() -> Unit) = {
        Log.d("Timer", "Timer launched")
        sendToHandheldDevice(timeSelection.intValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            WearApp(launchTimer, initialSliderPosition, timeSelection)
        }
    }

    override fun onGenericMotionEvent(ev: MotionEvent): Boolean {
        return if (ev.action == MotionEvent.ACTION_SCROLL &&
            ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
        ) {
            // Don't forget the negation here
            val delta = -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                    ViewConfigurationCompat.getScaledVerticalScrollFactor(
                        ViewConfiguration.get(this), this
                    )

            Log.d(TAG, "onGenericMotionEvent: $delta")

            initialSliderPosition.floatValue += delta / 13600

            if(initialSliderPosition.floatValue > 1f) {
                initialSliderPosition.floatValue = 1f
            } else if(initialSliderPosition.floatValue < 0f) {
                initialSliderPosition.floatValue = 0f
            }

            processTimeValue()

            true
        } else {
            false
        }
    }

    private fun processTimeValue() {
        val max = 90
        initialSliderPosition.floatValue.times(max).toInt().let {
            timeSelection.intValue = it
        }
    }

    private fun sendToHandheldDevice(time: Int) {
        try {
            messageClient.sendMessage(
                "com.jfalck.musictimer",
                "/timer",
                time.toString().toByteArray(charset = Charset.defaultCharset())
            ).apply {
                addOnSuccessListener {
                    Log.i(TAG, "sendMessage OnSuccessListener")
                }
                addOnFailureListener {
                    Log.i(TAG, "sendMessage OnFailureListener")
                }
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }
}

@Composable
fun WearApp(onLaunchTimer: () -> Unit, initialSliderPosition: MutableFloatState, timeSelection: MutableIntState) {

    val sliderPosition by remember { initialSliderPosition }
    val time by remember { timeSelection }

    MusicTimerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "time selected: $time minutes", modifier = Modifier.padding(16.dp), color = MaterialTheme.colors.secondary)
                TimerButton(onLaunchTimer)
            }
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colors.primary,
                indicatorColor = MaterialTheme.colors.secondary,
                progress = sliderPosition
            )
        }
    }
}

@Composable
fun TimerButton(onClick: () -> Unit) {
    Chip(
        colors = ChipDefaults.chipColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        ),
        modifier = Modifier.wrapContentSize(),
        onClick = { onClick() },
        label = {
            Text(
                text = stringResource(id = R.string.start_timer),
                maxLines = 1,
                color = MaterialTheme.colors.onPrimary,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Timer,
                tint = MaterialTheme.colors.onPrimary,
                contentDescription = "triggers meditation action",
                //modifier = iconModifier
            )
        },
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp({}, mutableFloatStateOf(0.5f), mutableIntStateOf(0))
}