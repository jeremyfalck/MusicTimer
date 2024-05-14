package com.jfalck.musictimer_common.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "MusicTimerWearableMessageSender"

class MusicTimerWearableMessageSender(context: Context) {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    fun sendWearableMessage(path: String, data: ByteArray? = null) {
        try {
            messageClient.sendMessage(
                "com.jfalck.musictimer",
                path,
                data
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