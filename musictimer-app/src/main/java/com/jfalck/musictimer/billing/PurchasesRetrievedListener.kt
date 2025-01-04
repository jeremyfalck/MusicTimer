package com.jfalck.musictimer.billing

import com.android.billingclient.api.Purchase

interface PurchasesRetrievedListener {
    fun onPurchasesRetrieved(purchases: List<Purchase>)
}