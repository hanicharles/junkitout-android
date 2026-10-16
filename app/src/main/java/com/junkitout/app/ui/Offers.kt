package com.junkitout.app.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkitout.app.data.Api
import com.junkitout.app.data.Offer
import com.junkitout.app.data.Result

@Composable
fun OffersScreen(
    token: String,
    onBack: () -> Unit,
    onApplyCoupon: ((Offer) -> Unit)? = null
) {
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var loading by remember { mutableStateOf(true) }
    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val r = Api.getOffers(token)) {
            is Result.Ok -> { offers = r.value; error = null }
            is Result.Err -> error = r.message
        }
        loading = false
    }

    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Offers & Coupons", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Save more on your pickups", fontSize = 13.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Exclusive Hero Banner
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0A3A1C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF093318), Color(0xFF145C2F))))
                        .padding(20.dp)
                ) {
                    Column(Modifier.fillMaxWidth(0.68f)) {
                        Text(
                            "Exclusive\nOffers for\na Cleaner World",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Use coupons and save on your next pickup.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Text("🎁", fontSize = 72.sp, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Available Offers", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = JColors.Text)

            Spacer(Modifier.height(12.dp))

            when {
                loading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(28.dp))
                }
                error != null -> Text(error!!, color = JColors.Red, fontSize = 13.sp)
                offers.isEmpty() -> Text("New offers are on the way!", color = JColors.Subtle, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    offers.forEach { offer ->
                        OfferCard(
                            offer = offer,
                            onApply = {
                                if (onApplyCoupon != null) {
                                    onApplyCoupon(offer)
                                } else {
                                    clipboard.setText(AnnotatedString(offer.code))
                                    Toast.makeText(ctx, "Coupon code '${offer.code}' copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // How to use Info Panel
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEAF6EC),
                border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFD5EDD8)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Lightbulb, null, tint = JColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("How to use?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                    }

                    Spacer(Modifier.height(10.dp))

                    val steps = listOf(
                        "1. Choose a valid coupon",
                        "2. Apply it at the time of booking",
                        "3. Enjoy the discount!"
                    )
                    steps.forEach { step ->
                        Text(step, fontSize = 12.sp, color = JColors.Text, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfferCard(offer: Offer, onApply: () -> Unit) {
    val iconBg = when (offer.code) {
        "RECYCLE20" -> Color(0xFFFDF0E8)
        "FREEDISPOSAL" -> Color(0xFFEDF5FD)
        "GREEN15" -> Color(0xFFF7FAEE)
        else -> Color(0xFFEAF6EC)
    }
    val iconTint = when (offer.code) {
        "RECYCLE20" -> Color(0xFFE57A2E)
        "FREEDISPOSAL" -> Color(0xFF2F80ED)
        "GREEN15" -> Color(0xFF3DA35D)
        else -> JColors.Primary
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, JColors.Border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(
                        when (offer.code) {
                            "RECYCLE20" -> Icons.Filled.Recycling
                            "FREEDISPOSAL" -> Icons.Filled.LocalShipping
                            "GREEN15" -> Icons.Filled.Eco
                            else -> Icons.Filled.LocalOffer
                        },
                        null, tint = iconTint, modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(offer.code, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                        if (offer.tag.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD5EDD8)) {
                                Text(offer.tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JColors.Primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(offer.description, fontSize = 12.sp, color = JColors.Text)
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border.copy(alpha = 0.6f)))
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Valid till ${offer.validTill}", fontSize = 11.sp, color = JColors.Subtle, modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JColors.Primary,
                    modifier = Modifier.clickable(onClick = onApply)
                ) {
                    Text("Apply", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp))
                }
            }
        }
    }
}
