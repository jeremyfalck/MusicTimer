package com.jfalck.musictimer.presenter.service.mute

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "MuteService"

class MuteService : Service() {

    companion object {
        const val TIME_IN_MINUTES = "TIME_IN_MINUTES"
    }

    private val muteBinder: MuteBinder by inject()

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Local intent received with action: ${intent.action}")
            if (intent.action == TimerNotificationManager.ACTION_STOP) {
                muteBinder.stopMuteTimer()
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val serviceType: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        CoroutineScope(Dispatchers.IO).launch {
            val timeSelectedInMinutes = intent.getIntExtra(TIME_IN_MINUTES, 0)
            muteBinder.buildNotification(timeSelectedInMinutes)?.let {
                ServiceCompat.startForeground(
                    this@MuteService, it.notificationId, it.notification, serviceType
                )
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service Binded")
        return muteBinder.apply {
            onStop = {
                ServiceCompat.stopForeground(
                    this@MuteService,
                    ServiceCompat.STOP_FOREGROUND_REMOVE
                )
            }
        }
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
}
