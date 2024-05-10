package com.jfalck.musictimer.service

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.jfalck.musictimer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


private const val TAG = "WearService"
private const val MESSAGE_PATH = "/timer"

class WearService : WearableListenerService() {

    private val muteBinder: MuteBinder by inject()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val time = messageEvent.data.decodeToString().toIntOrNull()
        Log.i(TAG, "onMessageReceived(): $time")
        when (messageEvent.path) {
            MESSAGE_PATH -> {
                Log.i(TAG, "Service: message (/timer) received: $time")

                if (time != null) {
                    muteBinder.startMuteTimer(time)
                    Toast.makeText(this@WearService, getString(R.string.timer_start_toast, time), Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "value is an empty string")
                }
            }
        }
    }
}
