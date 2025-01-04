package com.jfalck.musictimer.data.mapper

import com.android.billingclient.api.Purchase
import com.jfalck.musictimer.data.model.PurchaseModel

class PurchaseMapper {

    fun map(purchase: Purchase): PurchaseModel {
        return PurchaseModel(
            purchaseToken = purchase.purchaseToken,
            orderId = purchase.orderId,
            productId = purchase.products.first(),
            purchaseTime = purchase.purchaseTime,
            purchaseState = mapPurchaseState(purchase.purchaseState),
            quantity = purchase.quantity,
            acknowledged = purchase.isAcknowledged
        )
    }

    private fun mapPurchaseState(state: Int): String =
        when (state) {
            Purchase.PurchaseState.PURCHASED -> PurchaseModel.PURCHASED
            Purchase.PurchaseState.UNSPECIFIED_STATE -> PurchaseModel.UNSPECIFIED_STATE
            Purchase.PurchaseState.PENDING -> PurchaseModel.PENDING
            else -> PurchaseModel.UNSPECIFIED_STATE
        }

}