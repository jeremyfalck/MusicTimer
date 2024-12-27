package com.jfalck.musictimer.presenter.service.mute

interface MuteTimerManager {
    fun startMuteTimer(totalTimeInMinutes: Int)

    fun stopMuteTimer()
}
