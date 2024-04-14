package com.jfalck.killtimer

import android.content.Context

interface MuteTimerManager {
    fun startMuteTimer(time: Long, context: Context)
}
