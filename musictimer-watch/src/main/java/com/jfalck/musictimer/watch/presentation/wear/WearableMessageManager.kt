package com.jfalck.musictimer.watch.presentation.wear

import android.util.Log
import com.jfalck.musictimer_common.wear.MusicTimerWearableMessageSender
import java.nio.charset.Charset


private const val TAG = "WearableMessageManager"

class WearableMessageManager(private val musicTimerWearableMessageSender: MusicTimerWearableMessageSender) {

    fun startTimerOnPhone(time: Int) {
        Log.d(TAG, "startTimerOnPhone: $time")
        musicTimerWearableMessageSender.sendWearableMessage(
            "/timer",
            time.toString().toByteArray(charset = Charset.defaultCharset())
        )
    }

    fun stopTimerOnPhone() {
        Log.d(TAG, "stopTimerOnPhone")
        musicTimerWearableMessageSender.sendWearableMessage(
            "/stop_timer",
            null
        )
    }
}