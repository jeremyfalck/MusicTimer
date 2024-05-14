package com.jfalck.musictimer.watch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.jfalck.musictimer.watch.repository.TimerRunningRepository
import kotlinx.coroutines.flow.Flow

class TimerRunningViewModel(timerRunningRepository: TimerRunningRepository) :
    ViewModel() {

    var timerRunningLiveData: Flow<Boolean> = timerRunningRepository.isTimerRunning()

}