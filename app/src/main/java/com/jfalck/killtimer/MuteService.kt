package com.jfalck.killtimer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.koin.android.ext.android.inject

class MuteService : Service() {

    private var ctx: Context? = null

    private val muteBinder: MuteBinder by inject()

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service Binded")
        return muteBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Started")
        ctx = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Stopped")
    }

    companion object {
        private const val TAG = "MuteService"
    }
}
