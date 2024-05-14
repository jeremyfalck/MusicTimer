package com.jfalck.musictimer.presenter.service.mute

interface MuteTimerManager {
    fun startMuteTimer(timeInMinutes: Int)

    fun stopMuteTimer()
}
