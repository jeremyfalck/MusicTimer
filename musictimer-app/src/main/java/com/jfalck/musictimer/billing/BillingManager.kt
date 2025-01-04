package com.jfalck.musictimer.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.jfalck.musictimer.billing.Products.AD_FREE_PLAN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BillingManager"

class BillingManager(private val appContext: Context) {

    private var billingClient: BillingClient? = null

    fun initBillingClient(onPurchasesRetrievedListener: PurchasesRetrievedListener) {
        billingClient = BillingClient.newBuilder(appContext)
            .setListener { billingResult, purchases ->
                Log.d(TAG, "billingResult: $billingResult")
                Log.d(TAG, "purchases: $purchases")
                purchases?.let(onPurchasesRetrievedListener::onPurchasesRetrieved)
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()
    }

    fun getPurchases(onPurchasesRetrievedListener: PurchasesRetrievedListener) {
        val params: QueryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient?.queryPurchasesAsync(
            params
        ) { _, purchases ->
            onPurchasesRetrievedListener.onPurchasesRetrieved(purchases)
        }
    }

    fun startConnection(billingConnectionListener: BillingConnectionListener) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "billing setup finished successfully")
                    billingConnectionListener.onConnectionEstablished()
                } else {
                    billingConnectionListener.onConnectionFailed()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service is disconnected")
                billingConnectionListener.onConnectionLost()
            }
        })
    }

    fun showBillingDialog(activity: Activity) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(AD_FREE_PLAN)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        Log.d(TAG, "productList loaded: $productList")
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        CoroutineScope(Dispatchers.IO).launch {
            val productDetailResult: ProductDetailsResult? =
                billingClient?.queryProductDetails(params.build())
            Log.d(TAG, "products found: $productDetailResult")

            productDetailResult?.productDetailsList?.firstOrNull()
                ?.let { productDetails ->
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    CoroutineScope(Dispatchers.Main).launch {
                        billingClient?.launchBillingFlow(
                            activity,
                            billingFlowParams
                        )
                    }
                }
        }
    }

    suspend fun acknowledgePayment(purchaseToken: String) {
        billingClient?.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        ) { billingResult ->
            Log.d(
                TAG,
                "billingResult.responseCode: ${billingResult.responseCode}"
            )
        }
    }

}