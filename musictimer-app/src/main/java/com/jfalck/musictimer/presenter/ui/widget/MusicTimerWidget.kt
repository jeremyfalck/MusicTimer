package com.jfalck.musictimer.presenter.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.service.mute.MuteBinder
import com.jfalck.musictimer.presenter.service.mute.MuteService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MusicTimerWidget : GlanceAppWidget(), KoinComponent {

    private val muteBinder: MuteBinder by inject()

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

    @Composable
    private fun GlanceContent(context: Context) {
        val isTimerRunning = muteBinder.isTimerRunning.collectAsState()
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                text = context.getString(if (isTimerRunning.value) R.string.stop_timer else R.string.start_timer),
                onClick = {
                    if (isTimerRunning.value) {
                        muteBinder.stopMuteTimer()
                    } else {
                        actionStartService(MuteService::class.java)
                        muteBinder.startMuteTimer(20)
                    }
                }
            )
        }
    }
}