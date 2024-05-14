package com.jfalck.musictimer.presenter.service.mute

import android.annotation.SuppressLint
import android.os.Binder
import android.util.Log
import com.jfalck.musictimer.common.countdown.CustomCountDownTimer
import com.jfalck.musictimer.common.media.MediaFocusManager
import com.jfalck.musictimer.presenter.notification.TimerNotificationManager
import com.jfalck.musictimer.presenter.wear.WearableMessageManager
import com.jfalck.musictimer_common.data.DataStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "MuteBinder"

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class MuteBinder(
    private val timerNotificationManager: TimerNotificationManager,
    private val mediaFocusManager: MediaFocusManager,
    private val dataStoreManager: DataStoreManager,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val wearableMessageManager: WearableMessageManager
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
                mediaFocusManager.requestMediaFocus()
                _isTimerRunning.value = false
                wearableMessageManager.sendTimerState(false)
            }, isDevModeEnabled).apply {
                start()
                wearableMessageManager.sendTimerState(true)
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
        wearableMessageManager.sendTimerState(false)
        timerNotificationManager.clearNotification()
        _isTimerRunning.value = false
    }
}
