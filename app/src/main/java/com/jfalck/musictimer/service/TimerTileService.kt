package com.jfalck.musictimer.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.jfalck.musictimer.R
import org.koin.android.ext.android.inject

class TimerTileService : TileService() {

    private val muteBinder: MuteBinder by inject()

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        val isActive = qsTile.state != Tile.STATE_ACTIVE
        qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.label = if (isActive) "Timer ongoing" else getString(R.string.timer_tile_label)


        if(isActive) {
            muteBinder.startMuteTimer(20)
            Toast.makeText(this, getString(R.string.timer_start_toast, 20), Toast.LENGTH_SHORT).show()
        } else {
            muteBinder.stopMuteTimer()
        }

        qsTile.updateTile()
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }
}