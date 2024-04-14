package com.jfalck.musictimer.service

import android.annotation.SuppressLint
import android.os.Binder
import android.os.CountDownTimer
import android.util.Log
import com.jfalck.musictimer.MuteTimerManager
import com.jfalck.musictimer.manager.MuteManager
import com.jfalck.musictimer.notification.TimerNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MuteBinder(
    private val timerNotificationManager: TimerNotificationManager,
    private val muteManager: MuteManager
) : Binder(), MuteTimerManager {

    init {
        timerNotificationManager.createNotificationChannel()
    }

    private var countDownTimer: CountDownTimer? = null

    private var _isTimerRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isTimerRunning: StateFlow<Boolean> = _isTimerRunning

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
                _isTimerRunning.value = false
            }
        }

        countDownTimer?.start()
        _isTimerRunning.value = true

        timerNotificationManager.displayNotification(time)
    }

    override fun stopMuteTimer() {
        Log.d(TAG, "Timer task stopped")
        countDownTimer?.cancel()
        timerNotificationManager.clearNotification()
        _isTimerRunning.value = false
    }


    companion object {
        private const val TAG = "MuteBinder"
        private const val ONE_MINUTE_IN_MILLIS = 60000L
        private const val ONE_MINUTE = 1
    }
}