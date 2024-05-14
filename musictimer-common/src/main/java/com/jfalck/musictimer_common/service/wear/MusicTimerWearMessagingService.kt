package com.jfalck.musictimer_common.service.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.jfalck.musictimer_common.common.wear.IWearMessageProcessor
import org.koin.android.ext.android.inject


private const val TAG = "MusicTimerWearMessagingService"

class MusicTimerWearMessagingService :
    WearableListenerService() {

    private val wearMessageProcessor: IWearMessageProcessor by inject()
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        wearMessageProcessor.processMessage(messageEvent)
    }
}