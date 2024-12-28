package com.jfalck.musictimer.presenter.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import com.jfalck.musictimer.R
import com.jfalck.musictimer_common.data.CacheManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "TimerNotificationManager"

class TimerNotificationManager(
    private val context: Context,
    private val dataStoreManager: CacheManager,
    private val ioDispatcher: CoroutineDispatcher
) {

    @ColorInt
    private var notificationColor: Int? = null

    private val stopIntent = Intent(context, TimerBroadcastReceiver::class.java).apply {
        action = ACTION_STOP
        CoroutineScope(ioDispatcher).launch {
            putExtra(EXTRA_NOTIFICATION_ID, dataStoreManager.getNotificationId())
        }
    }
    private val stopPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

    @Composable
    fun SetPrimaryColor() {
        Log.d(TAG, "Setting primary color")
        notificationColor = MaterialTheme.colorScheme.primary.toArgb()
    }

    private val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
        setSmallIcon(R.drawable.ic_timer)
        setContentTitle(context.getString(R.string.app_name))
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
        setAutoCancel(false)
        setOngoing(true)
        setProgress(100, 0, false)
        addAction(
            NotificationCompat.Action(
                null,
                context.getString(R.string.stop),
                stopPendingIntent
            )
        )
    }

    private fun getNotification(timeRemaining: Int, totalTime: Int): Notification {
        return builder
            .setContentText(getRemainingTimeText(timeRemaining))
            .setProgress(100, ((totalTime - timeRemaining) * 100) / totalTime, false)
            .build()
    }

    private fun getRemainingTimeText(time: Int): String =
        context.getString(R.string.notification_message_remaining_time, time)

    @SuppressLint("MissingPermission")
    suspend fun createOrUpdateNotification(
        timeRemaining: Int,
        totalTime: Int
    ): NotificationResource? {
        Log.d(
            TAG,
            "displaying notification with $timeRemaining minutes remaining and $totalTime total minutes"
        )
        with(NotificationManagerCompat.from(context)) {
            if (!hasNotficationPermission()) {
                Log.d(TAG, "No permission to post notifications")
                return@with
            }
            if (timeRemaining == totalTime) {
                dataStoreManager.incrementNotificationId()
            }
            val notificationId = dataStoreManager.getNotificationId()
            Log.d(TAG, "displaying notification with id $notificationId")
            return NotificationResource(getNotification(timeRemaining, totalTime), notificationId);
        }
        return null
    }

    private fun hasNotficationPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun createNotificationChannel() {
        Log.d(TAG, "creating notification channel")
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    suspend fun sendNotification(timeRemainingInMinutes: Int, totalTimeInMinutes: Int) {
        val notificationId = dataStoreManager.getNotificationId()
        val notification = getNotification(timeRemainingInMinutes, totalTimeInMinutes)

        with(NotificationManagerCompat.from(context)) {
            if (!hasNotficationPermission()) {
                Log.d(TAG, "No permission to post notifications")
                return@with
            }
            notify(notificationId, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "MusicTimer"

        const val ACTION_STOP = "ACTION_STOP"
    }
}
