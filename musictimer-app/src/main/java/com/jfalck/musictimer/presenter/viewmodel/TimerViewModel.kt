package com.jfalck.musictimer.presenter.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer.usecase.GetLastTimeValueSelectedUseCase
import com.jfalck.musictimer.usecase.SetLastTimeValueSelectedUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "TimerViewModel"

class TimerViewModel(
    private val service: MuteBinder,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val getLastTimeValueSelectedUseCase: GetLastTimeValueSelectedUseCase,
    private val setLastTimeValueSelectedUseCase: SetLastTimeValueSelectedUseCase,
    private val muteServiceManager: MuteServiceManager
) : ViewModel() {

    val isTimerRunning: Flow<Boolean> = muteServiceManager.isTimerRunning

    private val _timeValueSelected: MutableStateFlow<Float> = MutableStateFlow(0f)
    val timeValueSelected: StateFlow<Float> = _timeValueSelected

    init {
        CoroutineScope(coroutineDispatcher).launch {
            getLastTimeValueSelectedUseCase().collect {
                Log.d(TAG, "Last time value selected: $it")
                _timeValueSelected.emit(it)
            }
        }
    }

    fun setTimeValueSelected(time: Float) {
        Log.d(TAG, "Setting time value selected: $time")
        _timeValueSelected.value = time
    }

    fun onStartTimer(time: Float) {
        Log.d(TAG, "Starting timer for $time minutes")
        CoroutineScope(coroutineDispatcher).launch {
            setLastTimeValueSelectedUseCase(time)
        }
    }

    fun stopMuteTimer(context: Context) =
        muteServiceManager.stopMuteService(context)
}