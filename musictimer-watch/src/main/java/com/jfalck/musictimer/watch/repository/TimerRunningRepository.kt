package com.jfalck.musictimer.watch.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TimerRunningRepository {

    private var timerRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun isTimerRunning(): Flow<Boolean> = timerRunning

    suspend fun setTimerRunning(isTimerRunning: Boolean) =
        timerRunning.emit(isTimerRunning)

}