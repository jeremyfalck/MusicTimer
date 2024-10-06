package com.jfalck.musictimer.presenter.service.mute

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection

class MuteServiceManager {
    fun startMuteService(context: Context, connection: ServiceConnection) {
        Intent(context, MuteService::class.java).apply {
            context.bindService(this, connection, Context.BIND_AUTO_CREATE)
            context.startService(this)
        }
    }

    fun stopMuteService(context: Context) =
        context.stopService(Intent(context, MuteService::class.java))
}