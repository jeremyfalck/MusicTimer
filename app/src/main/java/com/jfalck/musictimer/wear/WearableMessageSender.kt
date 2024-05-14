package com.jfalck.musictimer.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.nio.charset.Charset
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "WearableMessageSender"

class WearableMessageSender(context: Context) {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)


    fun sendTimerState(isRunning: Boolean) {
        try {
            messageClient.sendMessage(
                "com.jfalck.musictimer",
                "/timer_state",
                isRunning.toString().toByteArray(charset = Charset.defaultCharset())
            ).apply {
                addOnSuccessListener {
                    Log.i(TAG, "sendMessage OnSuccessListener")
                }
                addOnFailureListener {
                    Log.i(TAG, "sendMessage OnFailureListener")
                }
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

}