package com.jfalck.musictimer.wear

import android.util.Log
import com.jfalck.musictimer_common.wear.MusicTimerWearableMessageSender
import java.nio.charset.Charset

private const val TAG = "WearableMessageManager"

class WearableMessageManager(private val musicTimerWearableMessageSender: MusicTimerWearableMessageSender) {

    fun sendTimerState(isRunning: Boolean) {
        Log.d(TAG, "sendTimerState: $isRunning")
        musicTimerWearableMessageSender.sendWearableMessage(
            "/timer_state",
            isRunning.toString().toByteArray(charset = Charset.defaultCharset())
        )
    }
}