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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkitout.app.PaymentBus
import com.junkitout.app.data.Api
import com.junkitout.app.data.Booking
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
                if (!ok) onDone(false, msg)
                else scope.launch {
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
            Text("Pickup from $area", color = JColors.Subtle, fontSize = 13.sp)

            if (booked != null) {
                val b = booked!!
                Spacer(Modifier.height(30.dp))
                Box(Modifier.align(Alignment.CenterHorizontally).size(84.dp).clip(CircleShape).background(JColors.Primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Pickup Scheduled!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JColors.Text,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("${b.service}\n${b.date} - ${b.slot}\nEstimated ₹%.0f".format(b.amount), color = JColors.Subtle,
                    fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                info?.let { Spacer(Modifier.height(10.dp)); Text(it, color = JColors.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Pay ₹%.0f now".format(b.amount), arrow = false) { info = null; pay(b) }
                Spacer(Modifier.height(10.dp))
                OutlinedChip("Pay at pickup", true, Modifier.fillMaxWidth()) { onDone() }
                return@Column
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
            PrimaryButton(if (busy) "Booking..." else "Confirm Booking", arrow = false, enabled = !busy) {
                busy = true; error = null
                scope.launch {
                    when (val r = Api.createBooking(service, days[dayIdx].first, slot, kg, token)) {
                        is Result.Ok -> { busy = false; booked = r.value.copy(date = days[dayIdx].second, slot = slot) }
                        is Result.Err -> { busy = false; error = r.message }
                    }
                }
            }
        }
    }
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
    LaunchedEffect(reload) {
        loading = true
        when (val r = Api.bookings(token)) {
            is Result.Ok -> { list = r.value; error = null }
            is Result.Err -> error = r.message
        }
        loading = false
    }
    val pay = rememberPayment(token, phone, startPayment) { ok, msg -> info = msg; if (ok) reload++ }
    val shown = if (walletMode) list.filter { it.paid } else list

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (walletMode) "Wallet & Payments" else "My Orders", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
        if (walletMode) Text("Total paid: ₹%.0f".format(shown.sumOf { it.amount }), color = JColors.Subtle, fontSize = 13.sp)
        info?.let { Text(it, color = JColors.Primary, fontSize = 13.sp) }
        Spacer(Modifier.height(12.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = JColors.Primary) }
            error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error!!, color = JColors.Red, fontSize = 13.sp); Spacer(Modifier.height(8.dp))
                PrimaryButton("Retry", arrow = false) { reload++ }
            }
            shown.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (walletMode) "No payments yet" else "No orders yet. Tap Book to schedule a pickup.", color = JColors.Subtle)
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shown) { b ->
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row {
                                Text(b.service, fontWeight = FontWeight.SemiBold, color = JColors.Text, modifier = Modifier.weight(1f))
                                Text("₹%.0f".format(b.amount), fontWeight = FontWeight.Bold, color = JColors.Primary)
                            }
                            Text("${b.date} ${b.slot}".trim(), color = JColors.Subtle, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (b.paid) "Paid" else b.status, fontSize = 12.sp,
                                    color = if (b.paid) JColors.Primary else JColors.Subtle, modifier = Modifier.weight(1f))
                                if (!b.paid)
                                    Text("Pay now", color = JColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                        modifier = Modifier.clickable { info = null; pay(b) })
                            }
                        }
                    }
                }
            }
        }
    }
}
