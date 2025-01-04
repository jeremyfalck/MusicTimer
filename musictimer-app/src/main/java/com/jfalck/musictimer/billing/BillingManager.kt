package com.jfalck.musictimer.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BillingManager"

class BillingManager(private val appContext: Context) {

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d(TAG, "billingResult: $billingResult")
            Log.d(TAG, "purchases: $purchases")
        }

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun showBillingDialog(activity: Activity) {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "billingResult is OK")

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("ad_free_plan")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                    Log.d(TAG, "productList loaded: $productList")
                    val params = QueryProductDetailsParams.newBuilder()
                    params.setProductList(productList)

                    CoroutineScope(Dispatchers.IO).launch {
                        val productDetailResult: ProductDetailsResult =
                            billingClient.queryProductDetails(params.build())
                        Log.d(TAG, "products found: $productDetailResult")

                        productDetailResult.productDetailsList?.firstOrNull()
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
                                    billingClient.launchBillingFlow(
                                        activity,
                                        billingFlowParams
                                    )
                                }
                            }
                    }

                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service is disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

}