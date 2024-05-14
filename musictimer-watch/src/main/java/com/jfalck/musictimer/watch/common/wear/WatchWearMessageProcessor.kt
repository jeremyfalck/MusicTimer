package com.jfalck.musictimer.watch.common.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.jfalck.musictimer.watch.data.repository.TimerRunningRepository
import com.jfalck.musictimer_common.service.IWearMessageProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "WatchWearMessageProcessor"
private const val TIMER_RUNNING = "/timer_state"

class WatchWearMessageProcessor(private val timerRunningRepository: TimerRunningRepository) :
    IWearMessageProcessor {

    override fun processMessage(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            TIMER_RUNNING -> {
                val isTimerRunning = messageEvent.data.decodeToString().toBooleanStrictOrNull()
                Log.i(TAG, "message ($TIMER_RUNNING) received: $isTimerRunning")

                if (isTimerRunning != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        timerRunningRepository.setTimerRunning(isTimerRunning)
                    }
                } else {
                    Log.i(TAG, "value is an empty string")
                }
            }
        }
    }
}
