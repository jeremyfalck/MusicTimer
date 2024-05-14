package com.jfalck.musictimer.presenter.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager.Companion.ACTION_STOP

class TimerBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Intent received with action: ${intent.action}")
        if (intent.action == ACTION_STOP) {
            val localIntent = Intent(ACTION_STOP)
            context.sendBroadcast(localIntent)
        }
    }

    companion object {
        private const val TAG = "MyBroadcastReceiver"
    }
}
