package com.jfalck.musictimer.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.jfalck.musictimer.notification.TimerNotificationManager
import org.koin.android.ext.android.inject

class MuteService : Service() {

    private val muteBinder: MuteBinder by inject()

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Local intent received with action: ${intent.action}")
            if (intent.action == TimerNotificationManager.ACTION_STOP) {
                muteBinder.stopMuteTimer()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service Binded")
        return muteBinder
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Started")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                localBroadcastReceiver,
                IntentFilter(TimerNotificationManager.ACTION_STOP),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                localBroadcastReceiver,
                IntentFilter(TimerNotificationManager.ACTION_STOP)
            )
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "Service Stopped")
        unregisterReceiver(localBroadcastReceiver)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MuteService"
    }
}
