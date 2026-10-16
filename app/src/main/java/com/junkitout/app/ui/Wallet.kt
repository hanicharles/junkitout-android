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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import com.junkitout.app.data.WalletData
import com.junkitout.app.data.WalletTransaction
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
    token: String,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    startPayment: (orderId: String, amountPaise: Long, phone: String) -> Unit = { _, _, _ -> }
) {
    val ctx = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var wallet by remember { mutableStateOf(WalletData(250.0, 1250.0, 100.0, 8, 2)) }
    var transactions by remember { mutableStateOf<List<WalletTransaction>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }

    var showTopupDialog by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf<String?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var topupAmountText by remember { mutableStateOf("200") }
    var busyTopup by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(reload) {
        loading = true
        when (val r = Api.getWallet(token)) {
            is Result.Ok -> { wallet = r.value; error = null }
            is Result.Err -> error = r.message
        }
        when (val r = Api.getWalletTransactions(token)) {
            is Result.Ok -> transactions = r.value
            is Result.Err -> {}
        }
        loading = false
    }

    if (showNoticeDialog != null) {
        val title = showNoticeDialog!!
        AlertDialog(
            onDismissRequest = { showNoticeDialog = null },
            icon = { Icon(Icons.Filled.Info, null, tint = JColors.Primary, modifier = Modifier.size(32.dp)) },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Direct money transfers and bank withdrawals require regulated payment partner authorization under RBI guidelines.\n\nFeature is currently in preview for registered business accounts.",
                    fontSize = 13.sp, color = JColors.Subtle
                )
            },
            confirmButton = {
                TextButton(onClick = { showNoticeDialog = null }) { Text("Got it") }
            }
        )
    }

    if (showTopupDialog) {
        AlertDialog(
            onDismissRequest = { showTopupDialog = false },
            title = { Text("Top Up Wallet", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Enter amount to add to wallet:", fontSize = 13.sp, color = JColors.Subtle)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = topupAmountText,
                        onValueChange = { topupAmountText = it.filter { c -> c.isDigit() } },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = JColors.Text) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JColors.Primary,
                            unfocusedBorderColor = JColors.Border
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = listOf("100", "200", "500", "1000")
                        presets.forEach { amt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (topupAmountText == amt) Color(0xFFEAF6EC) else Color.White,
                                border = BorderStroke(1.dp, if (topupAmountText == amt) JColors.Primary else JColors.Border),
                                modifier = Modifier.weight(1f).clickable { topupAmountText = amt }
                            ) {
                                Text("₹$amt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                                    color = if (topupAmountText == amt) JColors.Primary else JColors.Text,
                                    modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = topupAmountText.toIntOrNull()?.let { it >= 10 } == true && !busyTopup,
                    onClick = {
                        val amt = topupAmountText.toDoubleOrNull() ?: 100.0
                        busyTopup = true
                        scope.launch {
                            when (val r = Api.createWalletTopupOrder(amt, token)) {
                                is Result.Ok -> {
                                    busyTopup = false
                                    showTopupDialog = false
                                    startPayment(r.value.orderId, r.value.amountPaise, "")
                                }
                                is Result.Err -> {
                                    busyTopup = false
                                    Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) { Text(if (busyTopup) "Creating..." else "Proceed to Pay") }
            },
            dismissButton = {
                TextButton(onClick = { showTopupDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Transaction History", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(Modifier.fillMaxWidth().height(320.dp).verticalScroll(rememberScrollState())) {
                    transactions.forEach { tx ->
                        TransactionRow(tx)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showHistoryDialog = false }) { Text("Close") } }
        )
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
                    Text("Wallet & Payments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Manage your wallet, payments and refunds", fontSize = 13.sp, color = JColors.Subtle)
                }
                IconButton(onClick = onOpenChat) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFEAF6EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.HelpOutline, "Help", tint = JColors.Primary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Wallet Balance Hero Card
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
                    Column(Modifier.fillMaxWidth(0.65f)) {
                        Text("Wallet Balance", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹%.2f".format(wallet.balance), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFFA2E8B5), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Safe & Secure", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                    Text("👛", fontSize = 76.sp, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4 Action Cards Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionCard(
                    title = "Add Money",
                    subtitle = "Top up wallet",
                    icon = Icons.Filled.Add,
                    iconBg = Color(0xFFEAF6EC),
                    iconTint = JColors.Primary,
                    modifier = Modifier.weight(1f)
                ) { showTopupDialog = true }

                ActionCard(
                    title = "Send Money",
                    subtitle = "To another user",
                    icon = Icons.Filled.Send,
                    iconBg = Color(0xFFEAF6EC),
                    iconTint = JColors.Primary,
                    modifier = Modifier.weight(1f)
                ) { showNoticeDialog = "Send Money" }

                ActionCard(
                    title = "Withdraw",
                    subtitle = "To bank account",
                    icon = Icons.Filled.AccountBalance,
                    iconBg = Color(0xFFEAF6EC),
                    iconTint = JColors.Primary,
                    modifier = Modifier.weight(1f)
                ) { showNoticeDialog = "Withdraw Money" }

                ActionCard(
                    title = "Payment History",
                    subtitle = "View all",
                    icon = Icons.Filled.History,
                    iconBg = Color(0xFFEDF5FD),
                    iconTint = Color(0xFF2F80ED),
                    modifier = Modifier.weight(1f)
                ) { showHistoryDialog = true }
            }

            Spacer(Modifier.height(16.dp))

            // Stats Summary Cards Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Total Spent Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEAF6EC),
                    border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Wallet, null, tint = JColors.Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Total Spent", fontSize = 11.sp, color = JColors.Subtle)
                            Text("₹%.2f".format(wallet.totalSpent), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                            Text("on ${wallet.ordersCount} orders", fontSize = 10.sp, color = JColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(16.dp))
                    }
                }

                // Total Refunded Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEDF5FD),
                    border = BorderStroke(1.dp, Color(0xFFD0E3F7)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Refresh, null, tint = Color(0xFF2F80ED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Total Refunded", fontSize = 11.sp, color = Color(0xFF2F80ED))
                            Text("₹%.2f".format(wallet.totalRefunded), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                            Text("on ${wallet.refundsCount} orders", fontSize = 10.sp, color = JColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Recent Transactions Section Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = JColors.Text, modifier = Modifier.weight(1f))
                Row(Modifier.clickable { showHistoryDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                    Text("View All", color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Primary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(28.dp))
                }
            } else if (transactions.isEmpty()) {
                Text("No transactions yet.", fontSize = 13.sp, color = JColors.Subtle, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transactions.take(5).forEach { tx ->
                        TransactionRow(tx)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bottom Sustainability Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEAF6EC),
                border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenChat)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌱", fontSize = 42.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Help make a greener city", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                        Spacer(Modifier.height(2.dp))
                        Text("Your payments support a cleaner and healthier environment.", fontSize = 11.sp, color = JColors.Subtle, lineHeight = 15.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JColors.Text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = JColors.Subtle, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isDebit = tx.type == "debit"
    val iconBg = if (isDebit) Color(0xFFEAF6EC) else Color(0xFFFDF0E8)
    val iconTint = if (isDebit) JColors.Primary else Color(0xFFE57A2E)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(
                    if (isDebit) Icons.Filled.ShoppingBag else Icons.Filled.Refresh,
                    null, tint = iconTint, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                if (tx.subtitle.isNotBlank()) {
                    Text(tx.subtitle, fontSize = 11.sp, color = JColors.Subtle)
                }
                Text(tx.date, fontSize = 10.sp, color = JColors.Subtle)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isDebit) "-" else "+"} ₹%.2f".format(tx.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(16.dp))
        }
    }
}
