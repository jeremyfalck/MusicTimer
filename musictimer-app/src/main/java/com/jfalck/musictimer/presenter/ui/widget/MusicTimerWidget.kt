package com.jfalck.musictimer.presenter.ui.widget

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.mute.MuteServiceManager
import com.jfalck.musictimer_common.data.CacheManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "MusicTimerWidget"

class MusicTimerWidget : GlanceAppWidget(), KoinComponent {

    private val muteServiceManager: MuteServiceManager by inject()
    private val dataStoreManager: CacheManager by inject()

    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
    }

    override val sizeMode =
        SizeMode.Responsive(setOf(SMALL_SQUARE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceContent(context)
        }
    }

    private var connection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service $name connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service $name disconnected")
        }
    }

    @Composable
    private fun GlanceContent(context: Context) {
        val isTimerRunning = muteServiceManager.isTimerRunning.collectAsState()
        val quickSettingsTimeValue =
            dataStoreManager.getQuickSettingsTimeValueFlow().collectAsState(0)
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                text = context.getString(if (isTimerRunning.value) R.string.stop_timer else R.string.start_timer),
                onClick = {
                    if (isTimerRunning.value) {
                        muteServiceManager.stopMuteService(context)
                    } else {
                        connection?.let {
                            muteServiceManager.startMuteService(
                                context,
                                it,
                                quickSettingsTimeValue.value
                            )
                        }
                    }
                }
            )
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        connection = null
        super.onDelete(context, glanceId)
    }
}