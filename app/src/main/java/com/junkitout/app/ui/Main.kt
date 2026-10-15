package com.junkitout.app.ui

import androidx.compose.runtime.LaunchedEffect
import com.junkitout.app.data.Address
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun MainScreen(
    area: String, city: String, phone: String, name: String, token: String,
    startPayment: (String, Long, String) -> Unit,
    onBook: (service: String?) -> Unit,
    onChangeLocation: () -> Unit, onAddAddress: () -> Unit, onNameChanged: (String) -> Unit, onLogout: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val ctx = LocalContext.current
    StatusBarIcons(darkIcons = true)

    Scaffold(
        containerColor = JColors.Bg,
        bottomBar = {
            BottomBar(
                selected = tab,
                onSelect = { tab = it },
                onBook = { onBook(null) }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeTab(area, city, onChangeLocation, onBook)
                1 -> OrdersTab(token, phone, startPayment)
                3 -> OrdersTab(token, phone, startPayment, walletMode = true)
                4 -> MenuTab(phone, name, token, onAddAddress, onNameChanged, onLogout, onOpenOrders = { tab = 1 }, onOpenWallet = { tab = 3 })
            }
        }
    }
}

// ───────────────────────── 11 Main Home ─────────────────────────
@Composable
private fun HomeTab(area: String, city: String, onChangeLocation: () -> Unit, onBook: (String?) -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(JColors.Light), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f).clickable(onClick = onChangeLocation)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(area, fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 15.sp)
                    Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(18.dp), tint = JColors.Text)
                }
                Text(city.ifBlank { "Bengaluru" }, color = JColors.Subtle, fontSize = 11.sp)
            }
            Box(Modifier.clickable { Toast.makeText(ctx, "No new notifications", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Filled.Notifications, "Notifications", tint = JColors.Text, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable { onBook(null) }) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = JColors.Text, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Search for services...", color = JColors.Subtle, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF0A3A1C), Color(0xFF1B7A3E))))
                .padding(18.dp)
        ) {
            Column {
                Text("Let's Recycle\nFor a Cleaner\nTomorrow", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF3DB35F))
                        .clickable { onBook(null) }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Book Now", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
            // Illustration placeholder – swap with your exported bin artwork
            Text("♻️", fontSize = 84.sp, modifier = Modifier.align(Alignment.CenterEnd))
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Our Services", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = JColors.Text, modifier = Modifier.weight(1f))
            Text("View All", color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onBook(null) })
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceTile(Icons.Filled.Devices, "E-Waste\nDisposal", Color(0xFF3B82A0), Modifier.weight(1f)) { onBook("E-Waste Disposal") }
            ServiceTile(Icons.Filled.Delete, "Household\nDry Waste", Color(0xFFE59A2E), Modifier.weight(1f)) { onBook("Household Dry Waste") }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceTile(Icons.Filled.Description, "Paper &\nCardboard", Color(0xFF3DA35D), Modifier.weight(1f)) { onBook("Paper & Cardboard") }
            ServiceTile(Icons.Filled.LocalDrink, "Plastic\nWaste", Color(0xFF2F80ED), Modifier.weight(1f)) { onBook("Plastic Waste") }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ServiceTile(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = JColors.Text, lineHeight = 15.sp)
        }
    }
}

// ───────────────────────── Bottom navigation ─────────────────────────
@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit, onBook: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Surface(
            color = Color.White, shadowElevation = 14.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 10.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom
            ) {
                NavItem(Icons.Filled.Home, "Home", selected == 0) { onSelect(0) }
                NavItem(Icons.Filled.Receipt, "Orders", selected == 1) { onSelect(1) }
                Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(24.dp))
                    Text("Book", fontSize = 11.sp, color = JColors.Primary, fontWeight = FontWeight.SemiBold)
                }
                NavItem(Icons.Filled.AccountBalanceWallet, "Wallet", selected == 3) { onSelect(3) }
                NavItem(Icons.Filled.Person, "Profile", selected == 4) { onSelect(4) }
            }
        }
        Box(
            Modifier.align(Alignment.TopCenter).size(62.dp).clip(CircleShape)
                .background(Color.White).padding(4.dp).clip(CircleShape)
                .background(JColors.Primary).clickable(onClick = onBook),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Autorenew, "Book", tint = Color.White, modifier = Modifier.size(30.dp)) }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val c = if (selected) JColors.Primary else JColors.Subtle
    Column(Modifier.width(64.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = c, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 11.sp, color = c, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// ───────────────────────── 12 App Menu ─────────────────────────
@Composable
private fun MenuTab(
    phone: String, name: String, token: String, onAddAddress: () -> Unit, onNameChanged: (String) -> Unit, onLogout: () -> Unit,
    onOpenOrders: () -> Unit, onOpenWallet: () -> Unit,
) {
    val shown = if (phone.length == 10) "+91 ${phone.take(5)} ${phone.drop(5)}" else "+91 $phone"
    val ctx = LocalContext.current
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var showAddresses by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf(name) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    if (editName) androidx.compose.material3.AlertDialog(
        onDismissRequest = { editName = false },
        title = { Text("Your name") },
        text = { androidx.compose.material3.OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, singleLine = true) },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                editName = false
                scope.launch {
                    when (val r = Api.updateName(nameDraft.trim(), token)) {
                        is Result.Ok -> onNameChanged(r.value)
                        is Result.Err -> Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = { editName = false }) { Text("Cancel") } }
    )
    LaunchedEffect(showAddresses) { (Api.addresses(token) as? Result.Ok)?.let { addresses = it.value } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF0A3A1C), Color(0xFF145C2F))))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFD5EDD8)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, null, tint = JColors.Primary, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(name.ifBlank { "Junk-It-Out user" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(shown, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
            Icon(Icons.Filled.Edit, "Edit name", tint = Color.White,
                modifier = Modifier.clickable { nameDraft = name; editName = true })
        }
        Spacer(Modifier.height(14.dp))
        val toast = { m: String -> Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show() }
        val items = listOf<Triple<ImageVector, String, () -> Unit>>(
            Triple(Icons.Filled.LocationOn, "My Addresses", { showAddresses = !showAddresses }),
            Triple(Icons.Filled.Receipt, "My Orders", onOpenOrders),
            Triple(Icons.Filled.AccountBalanceWallet, "Wallet & Payments", onOpenWallet),
            Triple(Icons.Filled.Help, "Support", {
                ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_SENDTO,
                    android.net.Uri.parse("mailto:support@junkitout.in")).putExtra(android.content.Intent.EXTRA_SUBJECT, "Junk-It-Out support"))
            }),
            Triple(Icons.Filled.Info, "About Us", { toast("Junk-It-Out v1.0 - Cleaner homes, greener tomorrow") }),
        )
        for ((ic, label, action) in items) {
            Row(Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 15.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(ic, null, tint = JColors.Text, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(16.dp))
                Text(label, color = JColors.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border.copy(alpha = 0.6f)))
            if (label == "My Addresses" && showAddresses) {
                if (addresses.isEmpty()) Text("No saved addresses", color = JColors.Subtle, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                for (a in addresses) {
                    Text("${a.type}: ${a.house}, ${a.street}", color = JColors.Text, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
                Text("+ Add new address", color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onAddAddress).padding(12.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFCE6E6))
                .clickable(onClick = onLogout).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = JColors.Red)
            Spacer(Modifier.width(14.dp))
            Text("Logout", color = JColors.Red, fontWeight = FontWeight.SemiBold)
        }
    }
}
