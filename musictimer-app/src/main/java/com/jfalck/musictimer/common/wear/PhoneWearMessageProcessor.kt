package com.jfalck.musictimer.common.wear

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.MessageEvent
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer_common.common.wear.IWearMessageProcessor


private const val TAG = "PhoneWearMessageProcessor"
private const val MESSAGE_PATH = "/timer"
private const val TIMER_STOP_PATH = "/stop_timer"

class PhoneWearMessageProcessor(
    private val context: Context,
    private val muteBinder: MuteBinder,
    private val muteServiceManager: MuteServiceManager
) :
    IWearMessageProcessor {

    private var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service $name connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service $name disconnected")
        }
    }

    override fun processMessage(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            MESSAGE_PATH -> {
                val time = messageEvent.data.decodeToString().toIntOrNull()
                Log.i(TAG, "Service: message ($MESSAGE_PATH) received: $time")
                if (time != null) {
                    muteServiceManager.startMuteService(context, connection, time)
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
