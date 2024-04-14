package com.jfalck.killtimer.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jfalck.killtimer.R

class TimerNotificationManager(private val context: Context) {

//    var Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

    private var builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentTitle("KillTimer")
        .setContentText("KillTimer is running")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(false)

    fun displayNotification() {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(1, builder.build())
        }

    }

    fun createNotificationChannel() {
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


    private companion object {
        const val CHANNEL_ID = "KillTimer"
        const val DATASTORE_NAME = "KillTimerDataStore"
    }
}