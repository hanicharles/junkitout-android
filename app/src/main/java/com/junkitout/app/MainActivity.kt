package com.junkitout.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.junkitout.app.BuildConfig
import com.junkitout.app.ui.AppNav
import com.junkitout.app.ui.JunkItOutTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

/** Receives Razorpay results and forwards them to whichever screen started the payment. */
object PaymentBus {
    var onResult: ((ok: Boolean, orderId: String, paymentId: String, signature: String, msg: String) -> Unit)? = null
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Checkout.preload(applicationContext)

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissions.toTypedArray())

        setContent {
            JunkItOutTheme { AppNav(startPayment = ::startPayment) }
        }
    }

    private fun startPayment(orderId: String, amountPaise: Long, phone: String) {
        val key = BuildConfig.RAZORPAY_KEY_ID
        if (key.isBlank()) { PaymentBus.onResult?.invoke(false, "", "", "", "Razorpay key not configured"); return }
        val co = Checkout().apply { setKeyID(key) }
        val opts = JSONObject()
            .put("name", "Junk-It-Out")
            .put("description", "Pickup booking")
            .put("order_id", orderId)
            .put("currency", "INR")
            .put("amount", amountPaise)
            .put("prefill", JSONObject().put("contact", phone))
            .put("theme", JSONObject().put("color", "#14793A"))
            .put("method", JSONObject().put("upi", true).put("card", true).put("netbanking", true).put("wallet", true))
            .put("upi", JSONObject().put("flow", "intent"))
        runCatching { co.open(this, opts) }
            .onFailure { PaymentBus.onResult?.invoke(false, "", "", "", it.message ?: "Could not open Razorpay") }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, data: PaymentData?) {
        PaymentBus.onResult?.invoke(true, data?.orderId.orEmpty(), razorpayPaymentId.orEmpty(), data?.signature.orEmpty(), "")
    }

    override fun onPaymentError(code: Int, description: String?, data: PaymentData?) {
        val msg = runCatching {
            val json = JSONObject(description.orEmpty())
            val err = json.optJSONObject("error")
            err?.optString("description")?.takeIf { it != "undefined" && it.isNotBlank() }
                ?: err?.optString("reason")?.replace('_', ' ')?.replaceFirstChar { it.uppercase() }
                ?: err?.optString("code")
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: description?.takeIf { !it.startsWith("{") && it.isNotBlank() }
            ?: "Payment was cancelled or could not be processed"
        PaymentBus.onResult?.invoke(false, "", "", "", msg)
    }
}
