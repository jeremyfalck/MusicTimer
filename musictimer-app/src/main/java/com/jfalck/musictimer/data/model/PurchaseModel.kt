package com.jfalck.musictimer.data.model

data class PurchaseModel(
    val purchaseToken: String,
    val orderId: String?,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: String,
    val quantity: Int,
    val acknowledged: Boolean
) {
    companion object {
        const val PURCHASED = "PURCHASED"
        const val UNSPECIFIED_STATE = "UNSPECIFIED_STATE"
        const val PENDING = "PENDING"
    }
}
