package com.jfalck.musictimer.service

import android.annotation.SuppressLint
import android.os.Binder
import android.util.Log
import com.jfalck.musictimer.MuteTimerManager
import com.jfalck.musictimer.countdown.CustomCountDownTimer
import com.jfalck.musictimer.data.DataStoreManager
import com.jfalck.musictimer.manager.MuteManager
import com.jfalck.musictimer.notification.TimerNotificationManager
import com.jfalck.musictimer.wear.WearableMessageSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "MuteBinder"

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MuteBinder(
    private val timerNotificationManager: TimerNotificationManager,
    private val muteManager: MuteManager,
    private val dataStoreManager: DataStoreManager,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val wearableMessageSender: WearableMessageSender
) : Binder(), MuteTimerManager {

    init {
        timerNotificationManager.createNotificationChannel()
    }

    private var countDownTimer: CustomCountDownTimer? = null

    private var _isTimerRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    override fun startMuteTimer(timeInMinutes: Int) {
        Log.d(TAG, "@${hashCode()} Timer task started")

        CoroutineScope(coroutineDispatcher).launch {
            val isDevModeEnabled = dataStoreManager.getDevModeEnabled()

            countDownTimer?.let {
                Log.d(TAG, "@${this@MuteBinder.hashCode()} Cancelling previous countdown timer")
                it.cancel()
            }
            timerNotificationManager.clearNotification()

            countDownTimer = CustomCountDownTimer(timeInMinutes, { minutesUntilFinished ->
                Log.d(TAG, "@${this@MuteBinder.hashCode()} Timer task ticked")
                timerNotificationManager.createOrUpdateNotification(
                    minutesUntilFinished,
                    timeInMinutes
                )
            }, {
                timerNotificationManager.clearNotification()
                Log.d(TAG, "@${this@MuteBinder.hashCode()} Timer task executed")
                muteManager.requestMediaFocus()
                _isTimerRunning.value = false
                wearableMessageSender.sendTimerState(false)
            }, isDevModeEnabled).apply {
                start()
                wearableMessageSender.sendTimerState(true)
            }

            _isTimerRunning.value = true
        }
    }

    override fun stopMuteTimer() {
        Log.d(TAG, "@${hashCode()} Timer task stopped")
        countDownTimer?.let {
            Log.d(TAG, "@${hashCode()} Cancelling countdown timer")
            it.cancel()
        }
        wearableMessageSender.sendTimerState(false)
        timerNotificationManager.clearNotification()
        _isTimerRunning.value = false
    }
}
