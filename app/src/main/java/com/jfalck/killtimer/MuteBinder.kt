package com.jfalck.killtimer

import android.content.Context
import android.os.Binder
import android.util.Log
import com.jfalck.killtimer.notification.TimerNotificationManager
import java.util.Timer
import java.util.TimerTask

class MuteBinder(private val timerNotificationManager: TimerNotificationManager) : Binder(),
    MuteTimerManager {

    init {
        timerNotificationManager.createNotificationChannel()
    }

    private val timer = Timer()

    private inner class MainTask(private val context: Context) : TimerTask() {
        override fun run() {
            Log.d(TAG, "Timer task executed")
            MuteManager(context).init()
        }
    }

    override fun startMuteTimer(time: Long, context: Context) {
        timer.schedule(MainTask(context), time)
        timerNotificationManager.displayNotification()
    }


    companion object {
        private const val TAG = "MuteBinder"
    }
}