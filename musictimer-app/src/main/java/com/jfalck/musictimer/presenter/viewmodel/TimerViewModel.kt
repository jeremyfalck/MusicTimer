package com.jfalck.musictimer.presenter.viewmodel

import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer.usecase.GetLastTimeValueSelectedUseCase
import com.jfalck.musictimer.usecase.GetTileAdditionSuggestionUseCase
import com.jfalck.musictimer.usecase.IncrementLaunchCountUseCase
import com.jfalck.musictimer.usecase.SetLastTimeValueSelectedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "TimerViewModel"

class TimerViewModel(
    private val getLastTimeValueSelectedUseCase: GetLastTimeValueSelectedUseCase,
    private val setLastTimeValueSelectedUseCase: SetLastTimeValueSelectedUseCase,
    private val getTileAdditionSuggestionUseCase: GetTileAdditionSuggestionUseCase,
    private val incrementLaunchCountUseCase: IncrementLaunchCountUseCase,
    private val muteServiceManager: MuteServiceManager
) : ViewModel() {

    val isTimerRunning: Flow<Boolean> = muteServiceManager.isTimerRunning

    private val _timeValueSelected: MutableStateFlow<Float> = MutableStateFlow(1f)
    val timeValueSelected: StateFlow<Float> = _timeValueSelected

    private val _showTileAdditionSuggestion: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showTileAdditionSuggestion: StateFlow<Boolean> = _showTileAdditionSuggestion

    init {
        viewModelScope.launch {
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

    fun onStartTimer(context: Context, connection: ServiceConnection, time: Int) {
        Log.d(TAG, "Starting timer for $time minutes")
        muteServiceManager.startMuteService(context, connection, time)
        viewModelScope.launch {
            incrementLaunchCountUseCase()
            manageTileSuggestion()
            setLastTimeValueSelectedUseCase(time.toFloat())
        }
    }

    private suspend fun manageTileSuggestion() {
        val shouldSuggestTile = getTileAdditionSuggestionUseCase()
        Log.d(TAG, "should suggest tile: $shouldSuggestTile")
        _showTileAdditionSuggestion.value = shouldSuggestTile
    }


    fun stopMuteTimer(context: Context) =
        muteServiceManager.stopMuteService(context)
}