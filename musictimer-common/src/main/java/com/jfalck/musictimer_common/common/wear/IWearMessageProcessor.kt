package com.jfalck.musictimer_common.common.wear

import com.google.android.gms.wearable.MessageEvent

interface IWearMessageProcessor {

    fun processMessage(messageEvent: MessageEvent)
}