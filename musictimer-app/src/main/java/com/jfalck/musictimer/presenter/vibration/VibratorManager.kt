package com.jfalck.musictimer.presenter.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibratorManager(context: Context) {

    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }
}