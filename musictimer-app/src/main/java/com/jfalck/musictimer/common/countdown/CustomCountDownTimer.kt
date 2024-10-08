package com.jfalck.musictimer.common.countdown

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


private const val TAG = "CustomCountDownTimer"

class CustomCountDownTimer(
    private val totalMinutes: Int,
    private val onTick: (Int) -> Unit = { minutesUntilFinished -> },
    private val onFinish: () -> Unit = { },
    private val useDebugSeconds: Boolean = true
) {

    private var job: Job? = null

    private val unit = if (useDebugSeconds) "seconds" else "minutes"

    private val flow =
        (totalMinutes - 1 downTo 0).asFlow() // Emit total - 1 because the first was emitted onStart
            .onEach {
                Log.d(TAG, "Emitting $unit: $it")
                delay(
                    if (useDebugSeconds) 1000 else 60000)
            } // Each minute later emit a number
            .onStart {
                Log.d(TAG, "Total $unit: $totalMinutes")
                emit(totalMinutes)
            } // Emit total seconds immediately
            .conflate() // In case the creating of State takes some time, conflate keeps the time ticking separately
            .cancellable()

    /**
     * The timer emits the total minutes immediately.
     * Each second after that, it will emit the next value.
     */
    fun start() {
        Log.d(TAG, "Starting timer")
        job = CoroutineScope(Dispatchers.IO).launch {
            flow.collect { minutes ->
                Log.d(TAG, "Remaining $unit: $minutes")
                if (minutes == 0) {
                    onFinish()
                } else {
                    onTick(minutes)
                }
            }
        }
    }

    fun cancel() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Cancelling timer")
            job?.cancelAndJoin()
        }
    }
}