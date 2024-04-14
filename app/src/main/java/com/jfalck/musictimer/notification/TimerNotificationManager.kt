package com.jfalck.musictimer.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import com.jfalck.musictimer.R
import com.jfalck.musictimer.TimerBroadcastReceiver
import com.jfalck.musictimer.data.DataStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TimerNotificationManager(
    private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val ioDispatcher: CoroutineDispatcher
) {

    private val stopIntent = Intent(context, TimerBroadcastReceiver::class.java).apply {
        action = ACTION_STOP
        CoroutineScope(ioDispatcher).launch {
            putExtra(EXTRA_NOTIFICATION_ID, dataStoreManager.getNotificationId())
        }
    }
    private val stopPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

    private val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentTitle("MusicTimer")
        .setContentText("MusicTimer is running")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(false)
        .setOngoing(true)
        .addAction(
            NotificationCompat.Action(
                null,
                context.getString(R.string.stop),
                stopPendingIntent
            )
        )

    fun displayNotification(time: Int) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "No permission to post notifications")
                return@with
            }
            builder.setContentText(getRemainingTimeText(time))
            CoroutineScope(ioDispatcher).launch {
                dataStoreManager.incrementNotificationId()
                val id = dataStoreManager.getNotificationId()
                // notificationId is a unique int for each notification that you must define.
                Log.d(TAG, "displaying notification with id $id")
                notify(id, builder.build())
            }
        }
    }

    private fun getRemainingTimeText(time: Int): String =
        "Music will stop playing in $time minutes"

    fun createNotificationChannel() {
        Log.d(TAG, "creating notification channel")
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun updateNotificationWithTime(time: Int) {
        Log.d(TAG, "updating notification with time $time")
        builder.setContentText(getRemainingTimeText(time))
        CoroutineScope(ioDispatcher).launch {
            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "No permission to post notifications")
                    return@with
                }
                NotificationManagerCompat.from(context)
                    .notify(dataStoreManager.getNotificationId(), builder.build())
            }
        }
    }

    fun clearNotification() {
        Log.d(TAG, "clearing notification")
        CoroutineScope(ioDispatcher).launch {
            NotificationManagerCompat.from(context).cancel(dataStoreManager.getNotificationId())
        }
    }


    companion object {
        private const val TAG = "TimerNotificationManager"
        private const val CHANNEL_ID = "MusicTimer"

        const val ACTION_STOP = "ACTION_STOP"
    }
}
