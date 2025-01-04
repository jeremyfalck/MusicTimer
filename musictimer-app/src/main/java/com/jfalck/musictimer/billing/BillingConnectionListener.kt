package com.jfalck.musictimer.billing

interface BillingConnectionListener {
    fun onConnectionEstablished()
    fun onConnectionFailed()
    fun onConnectionLost()
}