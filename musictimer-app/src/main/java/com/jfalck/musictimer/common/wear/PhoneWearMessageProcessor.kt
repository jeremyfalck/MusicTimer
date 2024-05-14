package com.jfalck.musictimer.common.wear

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.MessageEvent
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer_common.service.IWearMessageProcessor


private const val TAG = "PhoneWearMessageProcessor"
private const val MESSAGE_PATH = "/timer"
private const val TIMER_STOP_PATH = "/stop_timer"

class PhoneWearMessageProcessor(private val context: Context, private val muteBinder: MuteBinder) :
    IWearMessageProcessor {

    override fun processMessage(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            MESSAGE_PATH -> {
                val time = messageEvent.data.decodeToString().toIntOrNull()
                Log.i(TAG, "Service: message ($MESSAGE_PATH) received: $time")
                if (time != null) {
                    muteBinder.startMuteTimer(time)
                    Toast.makeText(
                        context,
                        context.getString(R.string.timer_start_toast, time),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.i(TAG, "value is an empty string")
                }
            }

            TIMER_STOP_PATH -> {
                Log.i(TAG, "Service: message ($TIMER_STOP_PATH) received")
                muteBinder.stopMuteTimer()

            }
        }
    }
}
