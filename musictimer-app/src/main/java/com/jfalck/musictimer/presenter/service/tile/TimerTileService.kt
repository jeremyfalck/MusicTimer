package com.jfalck.musictimer.presenter.service.tile

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "TimerTileService"

class TimerTileService : TileService() {

    private val muteBinder: MuteBinder by inject()
    private val muteServiceManager: MuteServiceManager by inject()
    private var timerListeningJob: Job? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service $name connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service $name disconnected")
        }
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        Log.d(TAG, "Starting listening")
        super.onStartListening()
        timerListeningJob = CoroutineScope(Dispatchers.IO).launch {
            muteBinder.isTimerRunning.collectLatest { isActive ->
                Log.d(TAG, "Timer running: $isActive")
                updateTile(isActive)
            }
        }
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        Log.d(TAG, "Stopping listening")
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        val isActive = qsTile.state != Tile.STATE_ACTIVE
        updateTile(isActive)


        if (isActive) {
            Log.d(TAG, "Starting timer service")
            muteServiceManager.startMuteService(this, connection)
            muteBinder.startMuteTimer(20)
            Toast.makeText(this, getString(R.string.timer_start_toast, 20), Toast.LENGTH_SHORT)
                .show()
        } else {
            Log.d(TAG, "Stopping timer service")
            muteServiceManager.stopMuteService(this)
            muteBinder.stopMuteTimer()
        }
    }

    private fun updateTile(isActive: Boolean) {
        qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.label = if (isActive) "Timer ongoing" else getString(R.string.timer_tile_label)
        qsTile.updateTile()
    }

    override fun onDestroy() {
        Log.d(TAG, "Service Stopped")
        CoroutineScope(Dispatchers.IO).launch {
            timerListeningJob?.cancelAndJoin()
        }
        super.onDestroy()
    }
}