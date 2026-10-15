package com.junkitout.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkitout.app.PaymentBus
import com.junkitout.app.data.Address
import com.junkitout.app.data.Api
import com.junkitout.app.data.Booking
import com.junkitout.app.data.Geo
import com.junkitout.app.data.Place
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val SERVICES = listOf("E-Waste Disposal", "Household Dry Waste", "Paper & Cardboard", "Plastic Waste")
private val SLOTS = listOf("9 AM - 12 PM", "12 PM - 3 PM", "3 PM - 6 PM")

/** Starts Razorpay for an existing booking: backend creates the order, checkout runs, backend verifies the signature. */
@Composable
fun rememberPayment(
    token: String, phone: String,
    startPayment: (orderId: String, amountPaise: Long, phone: String) -> Unit,
    onDone: (ok: Boolean, message: String) -> Unit,
): (Booking) -> Unit {
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<String?>(null) }
    DisposableEffect(pending) {
        val id = pending
        PaymentBus.onResult = { ok, orderId, paymentId, signature, msg ->
            if (id != null) {
                if (!ok) {
                    scope.launch {
                        Api.updateBookingStatus(id, "Payment Cancelled", token)
                        onDone(false, if (msg.contains("{")) "Payment was cancelled" else msg)
                    }
                } else scope.launch {
                    when (val r = Api.verifyPayment(id, orderId, paymentId, signature, token)) {
                        is Result.Ok -> onDone(true, "Payment successful")
                        is Result.Err -> onDone(false, "Paid, but verification failed: ${r.message}")
                    }
                }
                pending = null
            }
        }
        onDispose { PaymentBus.onResult = null }
    }
    return { booking ->
        scope.launch {
            when (val r = Api.createPaymentOrder(booking.id, token)) {
                is Result.Ok -> { pending = booking.id; startPayment(r.value.orderId, r.value.amountPaise, phone) }
                is Result.Err -> onDone(false, r.message)
            }
        }
    }
}

// ───────────────────────── Book a pickup ─────────────────────────
@Composable
fun BookingScreen(
    initialService: String?, token: String, phone: String, area: String,
    startPayment: (String, Long, String) -> Unit,
    onBack: () -> Unit, onDone: () -> Unit,
) {
    var service by remember { mutableStateOf(initialService ?: SERVICES[0]) }
    var dayIdx by remember { mutableIntStateOf(0) }
    var slot by remember { mutableStateOf(SLOTS[0]) }
    var kg by remember { mutableIntStateOf(5) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var booked by remember { mutableStateOf<Booking?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var selectedAddressText by remember { mutableStateOf(area.ifBlank { "Select pickup address" }) }
    var selectedAddressId by remember { mutableStateOf("") }
    var showAddressPicker by remember { mutableStateOf(false) }

    // Load saved default address if available
    LaunchedEffect(Unit) {
        (Api.addresses(token) as? Result.Ok)?.value?.let { list ->
            list.firstOrNull { it.isDefault }?.let { def ->
                selectedAddressId = def.id
                selectedAddressText = "${def.type}: ${def.house}, ${def.street}"
            } ?: list.firstOrNull()?.let { first ->
                selectedAddressId = first.id
                selectedAddressText = "${first.type}: ${first.house}, ${first.street}"
            }
        }
    }

    val days = remember {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val label = SimpleDateFormat("EEE d MMM", Locale.getDefault())
        (1..7).map { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it) } }.map { fmt.format(it.time) to label.format(it.time) }
    }
    val pay = rememberPayment(token, phone, startPayment) { ok, msg ->
        info = msg
        if (ok) onDone()
    }

    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            BackButton(onBack)
            Spacer(Modifier.height(14.dp))
            Text("Book a Pickup", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JColors.Text)

            if (booked != null) {
                val b = booked!!
                Spacer(Modifier.height(28.dp))
                Box(Modifier.align(Alignment.CenterHorizontally).size(80.dp).clip(CircleShape).background(if (b.paid) JColors.Primary else Color(0xFFE59A2E)), contentAlignment = Alignment.Center) {
                    Icon(if (b.paid) Icons.Filled.Check else Icons.Filled.Schedule, null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(if (b.paid) "Pickup Confirmed!" else "Payment Pending", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JColors.Text,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("${b.service}\n${b.date} - ${b.slot}\nEstimated ₹%.0f".format(b.amount), color = JColors.Subtle,
                    fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text("Address: $selectedAddressText", color = JColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                info?.let { Spacer(Modifier.height(10.dp)); Text(it, color = JColors.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

                Spacer(Modifier.height(24.dp))
                if (!b.paid) {
                    PrimaryButton("Pay ₹%.0f Now".format(b.amount), arrow = false) { info = null; pay(b) }
                } else {
                    PrimaryButton("View My Orders", arrow = false, onClick = onDone)
                }
                return@Column
            }

            // Pickup Address Card Section
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = JColors.Light,
                border = BorderStroke(1.dp, JColors.Border),
                modifier = Modifier.fillMaxWidth().clickable { showAddressPicker = true }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pickup Address", fontSize = 11.sp, color = JColors.Subtle, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text(selectedAddressText, fontSize = 14.sp, color = JColors.Text, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Change", color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(18.dp))
            Label("Service")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SERVICES.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { OutlinedChip(it, service == it, Modifier.weight(1f)) { service = it } }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Label("Pickup date")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                days.forEachIndexed { i, d -> OutlinedChip(d.second, dayIdx == i, Modifier.width(104.dp)) { dayIdx = i } }
            }
            Spacer(Modifier.height(18.dp))
            Label("Time slot")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SLOTS.forEach { OutlinedChip(it, slot == it, Modifier.weight(1f)) { slot = it } }
            }
            Spacer(Modifier.height(18.dp))
            Label("Approximate weight")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Stepper("−") { if (kg > 1) kg-- }
                Text("$kg kg", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = JColors.Text, modifier = Modifier.padding(horizontal = 24.dp))
                Stepper("+") { if (kg < 200) kg++ }
            }
            Spacer(Modifier.height(18.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = JColors.Light, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp)) {
                    Text("Estimated amount", color = JColors.Text, modifier = Modifier.weight(1f))
                    Text("₹%.0f".format(Api.estimate(service, kg)), fontWeight = FontWeight.Bold, color = JColors.Primary)
                }
            }
            Spacer(Modifier.height(22.dp))
            error?.let { Text(it, color = JColors.Red, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }
            PrimaryButton(if (busy) "Processing..." else "Proceed to Payment", arrow = true, enabled = !busy) {
                busy = true; error = null
                scope.launch {
                    when (val r = Api.createBooking(service, days[dayIdx].first, slot, kg, token, selectedAddressId, selectedAddressText)) {
                        is Result.Ok -> {
                            busy = false
                            val b = r.value.copy(date = days[dayIdx].second, slot = slot, addressText = selectedAddressText)
                            booked = b
                            pay(b)
                        }
                        is Result.Err -> { busy = false; error = r.message }
                    }
                }
            }
        }
    }

    if (showAddressPicker) {
        AddressSelectionModal(
            token = token,
            onDismiss = { showAddressPicker = false },
            onAddressSelected = { addrId, addrText ->
                selectedAddressId = addrId
                selectedAddressText = addrText
                showAddressPicker = false
            }
        )
    }
}

// ───────────────────────── Address Selection Modal ─────────────────────────
@Composable
private fun AddressSelectionModal(
    token: String,
    onDismiss: () -> Unit,
    onAddressSelected: (id: String, text: String) -> Unit
) {
    val ctx = LocalContext.current
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var mode by remember { mutableIntStateOf(0) } // 0: Select Saved, 1: Current Location, 2: Manual
    var house by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Home") }
    var busyLocation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        (Api.addresses(token) as? Result.Ok)?.value?.let { addresses = it }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Pickup Address", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == 0) Color(0xFFEAF6EC) else Color.White,
                        border = BorderStroke(1.dp, if (mode == 0) JColors.Primary else JColors.Border),
                        modifier = Modifier.weight(1f).clickable { mode = 0 }
                    ) {
                        Text("Saved", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                            color = if (mode == 0) JColors.Primary else JColors.Text, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == 1) Color(0xFFEAF6EC) else Color.White,
                        border = BorderStroke(1.dp, if (mode == 1) JColors.Primary else JColors.Border),
                        modifier = Modifier.weight(1f).clickable { mode = 1 }
                    ) {
                        Text("GPS", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                            color = if (mode == 1) JColors.Primary else JColors.Text, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == 2) Color(0xFFEAF6EC) else Color.White,
                        border = BorderStroke(1.dp, if (mode == 2) JColors.Primary else JColors.Border),
                        modifier = Modifier.weight(1f).clickable { mode = 2 }
                    ) {
                        Text("Manual", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                            color = if (mode == 2) JColors.Primary else JColors.Text, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                when (mode) {
                    0 -> { // Saved addresses
                        if (loading) CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                        else if (addresses.isEmpty()) Text("No saved addresses. Choose GPS or Manual to enter.", fontSize = 13.sp, color = JColors.Subtle)
                        else {
                            addresses.forEach { a ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF6FAF6),
                                    border = BorderStroke(1.dp, JColors.Border),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
                                        onAddressSelected(a.id, "${a.type}: ${a.house}, ${a.street}")
                                    }
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Text("${a.type}${if (a.isDefault) " [DEFAULT]" else ""}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JColors.Primary)
                                        Text("${a.house}, ${a.street}", fontSize = 12.sp, color = JColors.Text)
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Use GPS
                        if (busyLocation) Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Detecting current GPS location...", fontSize = 13.sp, color = JColors.Subtle)
                        } else {
                            PrimaryButton("Detect & Use Current GPS", arrow = false) {
                                busyLocation = true
                                scope.launch {
                                    val fix = Geo.currentFix(ctx)
                                    val place = if (fix != null) Geo.reverse(ctx, fix.latitude, fix.longitude) else null
                                    busyLocation = false
                                    if (place != null) {
                                        onAddressSelected("", "${place.area}, ${place.city}")
                                    } else {
                                        mode = 2 // Fallback to manual
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Manual
                        OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("House / Building No.") }, singleLine = true)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street / Area") }, singleLine = true)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark (Optional)") }, singleLine = true)
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("Use This Address", arrow = false, enabled = house.isNotBlank() && street.isNotBlank()) {
                            onAddressSelected("", "$house, $street")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable private fun Label(t: String) { Text(t, fontSize = 12.sp, color = JColors.Subtle, modifier = Modifier.padding(bottom = 8.dp)) }

@Composable
private fun OutlinedChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFFEAF6EC) else Color.White,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) JColors.Primary else JColors.Border),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(text, fontSize = 12.sp, textAlign = TextAlign.Center, color = if (selected) JColors.Primary else JColors.Text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp))
    }
}

@Composable
private fun Stepper(label: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(JColors.Light).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 22.sp, color = JColors.Primary, fontWeight = FontWeight.Bold)
    }
}

// ───────────────────────── Orders tab ─────────────────────────
@Composable
fun OrdersTab(token: String, phone: String, startPayment: (String, Long, String) -> Unit, walletMode: Boolean = false) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var list by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    var info by remember { mutableStateOf<String?>(null) }
    var filterTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reload) {
        loading = true
        when (val r = Api.bookings(token)) {
            is Result.Ok -> { list = r.value; error = null }
            is Result.Err -> error = r.message
        }
        loading = false
    }
    val pay = rememberPayment(token, phone, startPayment) { _, msg -> info = msg; reload++ }

    val rawList = if (walletMode) list.filter { it.paid } else list
    val shown = when (filterTab) {
        1 -> rawList.filter { !it.paid && it.status != "Cancelled" }
        2 -> rawList.filter { it.paid }
        3 -> rawList.filter { it.status == "Cancelled" || it.status == "Payment Cancelled" }
        else -> rawList
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (walletMode) "Wallet & Payments" else "My Orders", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
        if (walletMode) Text("Total paid: ₹%.0f".format(rawList.sumOf { it.amount }), color = JColors.Subtle, fontSize = 13.sp)

        info?.let {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = JColors.Light,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(10.dp))
            }
        }

        if (!walletMode && list.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filterLabels = listOf("All", "Upcoming", "Paid", "Cancelled")
                filterLabels.forEachIndexed { idx, label ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (filterTab == idx) Color(0xFFEAF6EC) else Color.White,
                        border = BorderStroke(if (filterTab == idx) 1.5.dp else 1.dp, if (filterTab == idx) JColors.Primary else JColors.Border),
                        modifier = Modifier.clickable { filterTab = idx }
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = if (filterTab == idx) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (filterTab == idx) JColors.Primary else JColors.Text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = JColors.Primary) }
            error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error!!, color = JColors.Red, fontSize = 13.sp); Spacer(Modifier.height(8.dp))
                PrimaryButton("Retry", arrow = false) { reload++ }
            }
            shown.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (walletMode) "No payments yet"
                    else when (filterTab) {
                        1 -> "No upcoming orders"
                        2 -> "No paid orders yet"
                        3 -> "No cancelled orders"
                        else -> "No orders yet. Tap Book to schedule a pickup."
                    },
                    color = JColors.Subtle
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(shown) { b ->
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(b.service, fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Text("₹%.0f".format(b.amount), fontWeight = FontWeight.Bold, color = JColors.Primary, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${b.date} • ${b.slot}".trim(), color = JColors.Subtle, fontSize = 13.sp)
                            if (b.addressText.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text("📍 ${b.addressText}", color = JColors.Subtle, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border.copy(alpha = 0.6f)))
                            Spacer(Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusText = when {
                                    b.paid -> "Paid Successfully"
                                    b.status == "Cancelled" -> "Cancelled"
                                    b.status == "Payment Cancelled" -> "Payment Cancelled"
                                    else -> b.status
                                }
                                val statusColor = when {
                                    b.paid -> JColors.Primary
                                    b.status == "Cancelled" || b.status == "Payment Cancelled" -> JColors.Red
                                    else -> Color(0xFF2F80ED)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.12f),
                                ) {
                                    Text(statusText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }

                                Spacer(Modifier.weight(1f))

                                if (!b.paid && b.status != "Cancelled") {
                                    Text("Cancel", color = JColors.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            info = null
                                            scope.launch {
                                                when (val r = Api.cancelBooking(b.id, token)) {
                                                    is Result.Ok -> { info = "Order cancelled"; reload++ }
                                                    is Result.Err -> info = r.message
                                                }
                                            }
                                        }.padding(horizontal = 8.dp, vertical = 4.dp))

                                    Spacer(Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = JColors.Primary,
                                        modifier = Modifier.clickable { info = null; pay(b) }
                                    ) {
                                        Text("Pay now", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
