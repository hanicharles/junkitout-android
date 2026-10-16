package com.junkitout.app.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import com.junkitout.app.data.WalletTransaction

@Composable
fun TransactionsScreen(
    token: String,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var transactions by remember { mutableStateOf<List<WalletTransaction>>(emptyList()) }
    var filterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Payments, 2: Refunds
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val r = Api.getWalletTransactions(token)) {
            is Result.Ok -> { transactions = r.value; error = null }
            is Result.Err -> error = r.message
        }
        loading = false
    }

    val shown = when (filterTab) {
        1 -> transactions.filter { it.type == "debit" }
        2 -> transactions.filter { it.type == "credit" }
        else -> transactions
    }

    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Transaction History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("View all your payments and refunds", fontSize = 13.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("All", "Payments", "Refunds")
                filters.forEachIndexed { idx, label ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (filterTab == idx) JColors.Primary else Color.White,
                        border = BorderStroke(1.dp, if (filterTab == idx) JColors.Primary else JColors.Border),
                        modifier = Modifier.clickable { filterTab = idx }
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (filterTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (filterTab == idx) Color.White else JColors.Text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(28.dp))
                }
                error != null -> Text(error!!, color = JColors.Red, fontSize = 13.sp)
                shown.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found.", color = JColors.Subtle, fontSize = 13.sp)
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(shown, key = { it.id }) { tx ->
                        TransactionCard(tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: WalletTransaction) {
    val isDebit = tx.type == "debit"
    val iconBg = if (isDebit) Color(0xFFEAF6EC) else Color(0xFFEDF5FD)
    val iconTint = if (isDebit) JColors.Primary else Color(0xFF2F80ED)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, JColors.Border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(
                    when {
                        tx.title.contains("Offer") -> Icons.Filled.LocalOffer
                        isDebit -> Icons.Filled.ShoppingBag
                        else -> Icons.Filled.Refresh
                    },
                    null, tint = iconTint, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                if (tx.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(tx.subtitle, fontSize = 11.sp, color = JColors.Subtle)
                }
                Spacer(Modifier.height(2.dp))
                Text(tx.date, fontSize = 10.sp, color = JColors.Subtle)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isDebit) "-" else "+"} ₹%.0f".format(tx.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = JColors.Text
                )
                Spacer(Modifier.height(4.dp))
                val statusBg = when (tx.status) {
                    "Completed" -> Color(0xFFEDF5FD)
                    "Processing" -> Color(0xFFFFF4E5)
                    "Failed" -> Color(0xFFFCE6E6)
                    else -> Color(0xFFEAF6EC)
                }
                val statusTint = when (tx.status) {
                    "Completed" -> Color(0xFF2F80ED)
                    "Processing" -> Color(0xFFE57A2E)
                    "Failed" -> JColors.Red
                    else -> JColors.Primary
                }
                Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                    Text(tx.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusTint, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
        }
    }
}
