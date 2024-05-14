package com.jfalck.musictimer.watch.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.jfalck.musictimer.watch.repository.TimerRunningRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


private const val TAG = "WearService"
private const val TIMER_RUNNING = "/timer_state"

class WearMessagingService : WearableListenerService() {

    private val timerRunningRepository: TimerRunningRepository by inject()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.i(TAG, "onMessageReceived(): $messageEvent")
        when (messageEvent.path) {
            TIMER_RUNNING -> {
                val isTimerRunning = messageEvent.data.decodeToString().toBooleanStrictOrNull()
                Log.i(TAG, "Service: message ($TIMER_RUNNING) received: $isTimerRunning")

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
