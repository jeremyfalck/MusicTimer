package com.jfalck.musictimer.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Binder
import android.os.CountDownTimer
import android.util.Log
import com.jfalck.musictimer.MuteTimerManager
import com.jfalck.musictimer.manager.MuteManager
import com.jfalck.musictimer.notification.TimerNotificationManager

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MuteBinder(
    private val context: Context,
    private val timerNotificationManager: TimerNotificationManager,
    private val muteManager: MuteManager
) : Binder(),
    MuteTimerManager {

    init {
        timerNotificationManager.createNotificationChannel()
    }

    private var countDownTimer: CountDownTimer? = null

    override fun startMuteTimer(time: Int) {

        val millis = time.toLong() * ONE_MINUTE_IN_MILLIS

        countDownTimer = object : CountDownTimer(millis, ONE_MINUTE_IN_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes: Int =
                    ((millisUntilFinished / ONE_MINUTE_IN_MILLIS) + ONE_MINUTE).toInt()
                timerNotificationManager.updateNotificationWithTime(minutes)
            }

            override fun onFinish() {
                timerNotificationManager.clearNotification()
                Log.d(TAG, "Timer task executed")
                muteManager.requestMediaFocus()
            }
        }

        countDownTimer?.start()

        timerNotificationManager.displayNotification(time)
    }

    override fun stopMuteTimer() {
        Log.d(TAG, "Timer task stopped")
        countDownTimer?.cancel()
        timerNotificationManager.clearNotification()
    }


    companion object {
        private const val TAG = "MuteBinder"
        private const val ONE_MINUTE_IN_MILLIS = 60000L
        private const val ONE_MINUTE = 1
    }
}