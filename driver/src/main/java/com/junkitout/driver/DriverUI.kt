package com.junkitout.driver

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import kotlinx.coroutines.launch

// ───────────────────────── Driver App Theme Colors ─────────────────────────
object DriverColors {
    val Primary = Color(0xFF0F5A2A)
    val Secondary = Color(0xFF2E9A50)
    val Bg = Color(0xFFF4FAF6)
    val CardBg = Color.White
    val Text = Color(0xFF10251C)
    val Subtle = Color(0xFF5A7365)
    val Border = Color(0xFFD5EDD8)
    val Red = Color(0xFFD32F2F)
    val Amber = Color(0xFFE57A2E)
}

// ───────────────────────── 1. Driver Login Screen ─────────────────────────
@Composable
fun DriverLoginScreen(
    onLoginSuccess: (token: String, driver: DriverInfo) -> Unit
) {
    val ctx = LocalContext.current
    var phone by remember { mutableStateOf("9591225555") }
    var name by remember { mutableStateOf("Collector Ramesh") }
    var vehicleNumber by remember { mutableStateOf("KA-01-EA-2026") }

    var step by remember { mutableIntStateOf(1) } // 1: Phone input, 2: OTP input
    var otp by remember { mutableStateOf("") }
    var seconds by remember { mutableIntStateOf(30) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(seconds, step) {
        if (step == 2 && seconds > 0) {
            kotlinx.coroutines.delay(1000L)
            seconds--
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0A3A1C), Color(0xFF0F5A2A), Color(0xFF145C2F))
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(2.dp, Color(0xFFB7D8BE)),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚛", fontSize = 52.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Junk-It-Out Partner", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Collection Executive & Driver Portal", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))

            Spacer(Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (step == 1) {
                        Text("Driver Partner Login", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DriverColors.Text)
                        Spacer(Modifier.height(4.dp))
                        Text("Sign in securely using your registered mobile number", fontSize = 12.sp, color = DriverColors.Subtle)

                        Spacer(Modifier.height(16.dp))

                        Text("Mobile Number *", fontSize = 12.sp, color = DriverColors.Subtle)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                            prefix = { Text("🇮🇳 +91 ", fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DriverColors.Primary)
                        )

                        Spacer(Modifier.height(20.dp))

                        error?.let {
                            Text(it, color = DriverColors.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DriverColors.Primary,
                            modifier = Modifier.fillMaxWidth().clickable(enabled = phone.length == 10 && !busy) {
                                step = 2
                                seconds = 30
                                Toast.makeText(ctx, "OTP sent to +91 $phone", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                Text("Send OTP →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    } else {
                        // Step 2: OTP Entry
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Verify Mobile Number", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DriverColors.Text)
                                Spacer(Modifier.height(4.dp))
                                Text("Enter 6-digit OTP sent to +91 $phone", fontSize = 12.sp, color = DriverColors.Subtle)
                            }
                            TextButton(onClick = { step = 1; otp = "" }) {
                                Text("Change", fontSize = 12.sp, color = DriverColors.Primary)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it.filter { c -> c.isDigit() }.take(6) },
                            placeholder = { Text("Enter 6-digit OTP (e.g. 123456)", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DriverColors.Primary)
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (seconds > 0) "Resend OTP in $seconds s" else "Didn't receive OTP?",
                                fontSize = 12.sp,
                                color = DriverColors.Subtle
                            )
                            if (seconds == 0) {
                                TextButton(onClick = { seconds = 30; Toast.makeText(ctx, "Resent OTP to +91 $phone", Toast.LENGTH_SHORT).show() }) {
                                    Text("Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DriverColors.Primary)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        error?.let {
                            Text(it, color = DriverColors.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DriverColors.Primary,
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !busy) {
                                busy = true
                                error = null
                                scope.launch {
                                    when (val r = DriverApi.login(phone, name, vehicleNumber)) {
                                        is DriverResult.Ok -> {
                                            busy = false
                                            onLoginSuccess(r.value.first, r.value.second)
                                        }
                                        is DriverResult.Err -> {
                                            busy = false
                                            error = r.message
                                        }
                                    }
                                }
                            }
                        ) {
                            Box(Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                Text(if (busy) "Logging in..." else "Verify OTP & Login →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 2. Driver Dashboard Screen ─────────────────────────
@Composable
fun DriverDashboardScreen(
    token: String,
    driver: DriverInfo,
    onLogout: () -> Unit
) {
    val ctx = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pickups, 1: Trips, 2: Earnings, 3: Profile
    var isOnline by remember { mutableStateOf(driver.status == "online") }
    var pickups by remember { mutableStateOf<List<PickupOrder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reload by remember { mutableIntStateOf(0) }

    var otpInputOrder by remember { mutableStateOf<PickupOrder?>(null) }
    var otpText by remember { mutableStateOf("") }
    var busyOtp by remember { mutableStateOf(false) }

    var weightInputOrder by remember { mutableStateOf<PickupOrder?>(null) }
    var weightText by remember { mutableStateOf("15") }
    var busyWeight by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var earnings by remember { mutableStateOf(Triple(0, 0, 0.0)) }

    LaunchedEffect(reload) {
        loading = true
        when (val r = DriverApi.getPickups(token)) {
            is DriverResult.Ok -> pickups = r.value
            is DriverResult.Err -> Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
        }
        when (val r = DriverApi.getEarnings(token)) {
            is DriverResult.Ok -> earnings = r.value
            is DriverResult.Err -> {}
        }
        loading = false
    }

    // Customer Pickup OTP Verification Dialog
    if (otpInputOrder != null) {
        val target = otpInputOrder!!
        AlertDialog(
            onDismissRequest = { otpInputOrder = null },
            icon = { Icon(Icons.Filled.Lock, null, tint = DriverColors.Primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Customer Pickup OTP", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Ask customer for the 4-digit Pickup Verification OTP shown on their order details:", fontSize = 12.sp, color = DriverColors.Subtle)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = otpText,
                        onValueChange = { otpText = it.filter { c -> c.isDigit() }.take(4) },
                        singleLine = true,
                        placeholder = { Text("Enter 4-digit OTP", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DriverColors.Primary)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = otpText.length == 4 && !busyOtp,
                    onClick = {
                        busyOtp = true
                        scope.launch {
                            when (val r = DriverApi.verifyOtp(target.id, otpText, token)) {
                                is DriverResult.Ok -> {
                                    busyOtp = false
                                    otpInputOrder = null
                                    otpText = ""
                                    Toast.makeText(ctx, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()
                                    weightInputOrder = target
                                    reload++
                                }
                                is DriverResult.Err -> {
                                    busyOtp = false
                                    Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) { Text(if (busyOtp) "Verifying..." else "Verify OTP") }
            },
            dismissButton = {
                TextButton(onClick = { otpInputOrder = null }) { Text("Cancel") }
            }
        )
    }

    // Actual Weight Entry Dialog
    if (weightInputOrder != null) {
        val target = weightInputOrder!!
        AlertDialog(
            onDismissRequest = { weightInputOrder = null },
            icon = { Icon(Icons.Filled.Scale, null, tint = DriverColors.Primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Enter Actual Weight (kg)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Enter total weighed waste collected at doorstep:", fontSize = 12.sp, color = DriverColors.Subtle)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.filter { c -> c.isDigit() } },
                        suffix = { Text("kg", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DriverColors.Primary)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = weightText.toIntOrNull()?.let { it > 0 } == true && !busyWeight,
                    onClick = {
                        val kg = weightText.toIntOrNull() ?: 10
                        busyWeight = true
                        scope.launch {
                            when (val r = DriverApi.completePickup(target.id, kg, token)) {
                                is DriverResult.Ok -> {
                                    busyWeight = false
                                    weightInputOrder = null
                                    Toast.makeText(ctx, "Pickup Completed Successfully!", Toast.LENGTH_SHORT).show()
                                    reload++
                                }
                                is DriverResult.Err -> {
                                    busyWeight = false
                                    Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) { Text(if (busyWeight) "Completing..." else "Complete Pickup") }
            },
            dismissButton = {
                TextButton(onClick = { weightInputOrder = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = DriverColors.Bg,
        bottomBar = {
            DriverBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DriverDutyTab(
                    driver = driver,
                    isOnline = isOnline,
                    onToggleOnline = { isOnline = it },
                    earnings = earnings,
                    pickups = pickups,
                    loading = loading,
                    onReload = { reload++ },
                    onNavigate = { pickup ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${pickup.addressText}"))
                        ctx.startActivity(intent)
                    },
                    onUpdateStatus = { pickup, newStatus ->
                        scope.launch {
                            when (val r = DriverApi.updateStatus(pickup.id, newStatus, token)) {
                                is DriverResult.Ok -> {
                                    Toast.makeText(ctx, "Status updated: $newStatus", Toast.LENGTH_SHORT).show()
                                    reload++
                                }
                                is DriverResult.Err -> Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onVerifyOtp = { pickup -> otpInputOrder = pickup },
                    onWeigh = { pickup -> weightInputOrder = pickup },
                    onAccept = { pickup ->
                        scope.launch {
                            when (val r = DriverApi.acceptPickup(pickup.id, token)) {
                                is DriverResult.Ok -> {
                                    Toast.makeText(ctx, "Pickup claimed successfully!", Toast.LENGTH_SHORT).show()
                                    reload++
                                }
                                is DriverResult.Err -> Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                1 -> DriverTripHistoryTab(pickups = pickups.filter { it.status == "Completed" || it.paid })
                2 -> DriverEarningsTab(earnings = earnings)
                3 -> DriverProfileTab(driver = driver, onLogout = onLogout)
            }
        }
    }
}

// ───────────────────────── Tab 0: Duty & Pickups Feed ─────────────────────────
@Composable
private fun DriverDutyTab(
    driver: DriverInfo,
    isOnline: Boolean,
    onToggleOnline: (Boolean) -> Unit,
    earnings: Triple<Int, Int, Double>,
    pickups: List<PickupOrder>,
    loading: Boolean,
    onReload: () -> Unit,
    onNavigate: (PickupOrder) -> Unit,
    onUpdateStatus: (PickupOrder, String) -> Unit,
    onVerifyOtp: (PickupOrder) -> Unit,
    onWeigh: (PickupOrder) -> Unit,
    onAccept: (PickupOrder) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Duty Status Header Bar
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFEAF6EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚛", fontSize = 24.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DriverColors.Text)
                    Text("${driver.vehicleNumber} • ${driver.serviceArea}", fontSize = 12.sp, color = DriverColors.Subtle)
                }
                Switch(
                    checked = isOnline,
                    onCheckedChange = onToggleOnline,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DriverColors.Primary)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Earnings Summary Quick Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DriverColors.Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Today's Earnings", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Text("₹%.2f".format(earnings.third), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("${earnings.first} Completed Pickups • ${earnings.second} kg Collected", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                }
                IconButton(onClick = onReload) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        val myAssigned = pickups.filter { it.isAssignedToMe && it.status != "Completed" }
        val unassignedFeed = pickups.filter { !it.isAssignedToMe && it.status == "Scheduled" }

        // Active Assigned Pickups
        if (myAssigned.isNotEmpty()) {
            Text("Your Active Pickups (${myAssigned.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DriverColors.Text)
            Spacer(Modifier.height(10.dp))

            myAssigned.forEach { pickup ->
                AssignedPickupCard(
                    pickup = pickup,
                    onNavigate = { onNavigate(pickup) },
                    onUpdateStatus = { newStatus -> onUpdateStatus(pickup, newStatus) },
                    onVerifyOtp = { onVerifyOtp(pickup) },
                    onWeigh = { onWeigh(pickup) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Unassigned Feed
        Text("Available Pickups Feed (${unassignedFeed.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DriverColors.Text)
        Spacer(Modifier.height(10.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DriverColors.Primary)
            }
        } else if (unassignedFeed.isEmpty()) {
            Text("No available pickups in your area currently.", fontSize = 13.sp, color = DriverColors.Subtle)
        } else {
            unassignedFeed.forEach { pickup ->
                UnassignedPickupCard(pickup = pickup, onAccept = { onAccept(pickup) })
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ───────────────────────── Tab 1: Trip History ─────────────────────────
@Composable
private fun DriverTripHistoryTab(pickups: List<PickupOrder>) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text("Trip History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DriverColors.Text)
        Text("Log of all completed waste pickups", fontSize = 13.sp, color = DriverColors.Subtle)

        Spacer(Modifier.height(18.dp))

        if (pickups.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No completed trips yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DriverColors.Text)
                    Spacer(Modifier.height(4.dp))
                    Text("Claim and complete pickups from the duty feed to build your trip history.", fontSize = 12.sp, color = DriverColors.Subtle, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pickups.forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, DriverColors.Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CheckCircle, null, tint = DriverColors.Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.service, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DriverColors.Text)
                                Text("📍 ${p.addressText}", fontSize = 12.sp, color = DriverColors.Subtle)
                                Text("📅 ${p.date} • ${p.estimatedKg} kg collected", fontSize = 11.sp, color = DriverColors.Subtle)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("₹%.0f".format(p.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DriverColors.Primary)
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Tab 2: Earnings & Payouts ─────────────────────────
@Composable
private fun DriverEarningsTab(earnings: Triple<Int, Int, Double>) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text("Earnings & Settlements", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DriverColors.Text)
        Text("Your daily collection payouts and incentives", fontSize = 13.sp, color = DriverColors.Subtle)

        Spacer(Modifier.height(18.dp))

        // Large Payout Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DriverColors.Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Total Settled Earnings", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(4.dp))
                Text("₹%.2f".format(earnings.third), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Pickups Completed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("${earnings.first} Trips", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Waste Collected", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("${earnings.second} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Payout Info Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DriverColors.Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Settlement Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DriverColors.Text)
                Spacer(Modifier.height(6.dp))
                Text("🏦 State Bank of India •••• 4082", fontSize = 13.sp, color = DriverColors.Text, fontWeight = FontWeight.SemiBold)
                Text("Automatic weekly direct deposit every Monday", fontSize = 11.sp, color = DriverColors.Subtle)
            }
        }
    }
}

// ───────────────────────── Tab 3: Driver Profile & Settings ─────────────────────────
@Composable
private fun DriverProfileTab(driver: DriverInfo, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text("Account & Vehicle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DriverColors.Text)
        Text("Collection Executive Partner Profile", fontSize = 13.sp, color = DriverColors.Subtle)

        Spacer(Modifier.height(18.dp))

        // Driver Info Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DriverColors.Border),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFEAF6EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = DriverColors.Primary, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DriverColors.Text)
                    Text("+91 ${driver.phone}", fontSize = 13.sp, color = DriverColors.Subtle)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEAF6EC)) {
                        Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = DriverColors.Primary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Verified Partner Executive", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DriverColors.Primary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Menu Items Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DriverColors.Border),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                val menuItems = listOf(
                    Triple(Icons.Filled.TwoWheeler, "Vehicle Registration", driver.vehicleNumber),
                    Triple(Icons.Filled.Edit, "Assigned Duty Area", driver.serviceArea),
                    Triple(Icons.Filled.Help, "Partner Support & Helpdesk", "Get help or contact operations"),
                    Triple(Icons.Filled.Settings, "App & Alert Settings", "Notification preferences"),
                    Triple(Icons.Filled.Info, "About Junk-It-Out Partner", "v1.0 Partner Ecosystem")
                )
                menuItems.forEachIndexed { idx, item ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            Toast.makeText(ctx, item.second, Toast.LENGTH_SHORT).show()
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                            Icon(item.first, null, tint = DriverColors.Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.second, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DriverColors.Text)
                            Text(item.third, fontSize = 11.sp, color = DriverColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = DriverColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                    if (idx < menuItems.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(DriverColors.Border.copy(alpha = 0.5f)))
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Logout Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFCE6E6),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onLogout)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = DriverColors.Red)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Logout Partner Account", color = DriverColors.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Sign out from collector session", color = DriverColors.Red.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ───────────────────────── Driver Bottom Navigation Bar ─────────────────────────
@Composable
private fun DriverBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DriverNavItem(Icons.Filled.LocalShipping, "Duty Feed", selected == 0) { onSelect(0) }
            DriverNavItem(Icons.Filled.History, "Trips", selected == 1) { onSelect(1) }
            DriverNavItem(Icons.Filled.AccountBalanceWallet, "Earnings", selected == 2) { onSelect(2) }
            DriverNavItem(Icons.Filled.Person, "Account", selected == 3) { onSelect(3) }
        }
    }
}

@Composable
private fun DriverNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) DriverColors.Primary else DriverColors.Subtle
    Column(
        Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AssignedPickupCard(
    pickup: PickupOrder,
    onNavigate: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onWeigh: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, DriverColors.Primary),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pickup.service, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DriverColors.Text, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEAF6EC)) {
                    Text(pickup.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DriverColors.Primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("👤 ${pickup.customerName} • 📞 ${pickup.customerPhone}", fontSize = 13.sp, color = DriverColors.Text, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("📍 ${pickup.addressText}", fontSize = 12.sp, color = DriverColors.Subtle)
            Text("📦 Est. Weight: ${pickup.estimatedKg} kg • Amount: ₹%.0f".format(pickup.amount), fontSize = 12.sp, color = DriverColors.Subtle)

            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(DriverColors.Border))
            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDF5FD),
                    border = BorderStroke(1.dp, Color(0xFFD0E3F7)),
                    modifier = Modifier.weight(1f).clickable(onClick = onNavigate)
                ) {
                    Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Navigation, null, tint = Color(0xFF2F80ED), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Navigate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F80ED))
                    }
                }

                if (pickup.status == "Accepted") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DriverColors.Primary,
                        modifier = Modifier.weight(1f).clickable { onUpdateStatus("En Route") }
                    ) {
                        Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text("En Route", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else if (pickup.status == "En Route") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE57A2E),
                        modifier = Modifier.weight(1f).clickable { onUpdateStatus("Arrived") }
                    ) {
                        Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text("Arrived", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DriverColors.Primary,
                        modifier = Modifier.weight(1f).clickable(onClick = onVerifyOtp)
                    ) {
                        Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Verify OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnassignedPickupCard(pickup: PickupOrder, onAccept: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, DriverColors.Border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pickup.service, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DriverColors.Text, modifier = Modifier.weight(1f))
                Text("₹%.0f".format(pickup.amount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DriverColors.Primary)
            }
            Spacer(Modifier.height(4.dp))
            Text("📍 ${pickup.addressText}", fontSize = 12.sp, color = DriverColors.Subtle)
            Text("📅 ${pickup.date} • ${pickup.slot} • ${pickup.estimatedKg} kg", fontSize = 11.sp, color = DriverColors.Subtle)

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DriverColors.Primary,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAccept)
            ) {
                Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text("Claim & Accept Pickup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
