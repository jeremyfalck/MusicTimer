package com.jfalck.musictimer.presenter

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.tile.TimerTileService

private const val TAG = "TileManager"

class TileManager(private val applicationContext: Context) {

    @SuppressLint("NewApi")
    fun suggestTile() {
        Log.d(TAG, "suggestTile()")
        val statusBarService =
            applicationContext.getSystemService(StatusBarManager::class.java)

        val componentName = ComponentName(
            applicationContext,
            TimerTileService::class.java.getName()
        )
        statusBarService.requestAddTileService(
            componentName,
            applicationContext.getString(R.string.timer_tile_label),
            Icon.createWithResource(
                applicationContext,
                R.drawable.timer_tile_icon
            ),
            {},
            {}
        )
    }
}