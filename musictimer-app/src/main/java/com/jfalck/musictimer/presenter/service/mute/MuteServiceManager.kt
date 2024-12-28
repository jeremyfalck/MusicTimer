package com.jfalck.musictimer.presenter.service.mute

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection

class MuteServiceManager(private val muteBinder: MuteBinder) {

    val isTimerRunning = muteBinder.isTimerRunning

    fun startMuteService(context: Context, connection: ServiceConnection, timeInMinutes: Int) {
        muteBinder.startMuteTimer(timeInMinutes)
        Intent(context, MuteService::class.java).apply {
            putExtra(MuteService.EXTRA_TIME_IN_MINUTES, timeInMinutes)
            context.bindService(this, connection, Context.BIND_AUTO_CREATE)
            context.startService(this)
        }
    }

    fun stopMuteService(context: Context) {
        muteBinder.stopMuteTimer()
        context.stopService(Intent(context, MuteService::class.java))
    }
}