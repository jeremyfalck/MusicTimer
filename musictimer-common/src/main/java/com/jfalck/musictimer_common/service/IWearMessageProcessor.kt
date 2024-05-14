package com.jfalck.musictimer_common.service

import com.google.android.gms.wearable.MessageEvent

interface IWearMessageProcessor {

    fun processMessage(messageEvent: MessageEvent)
}