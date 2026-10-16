package com.junkitout.app.ui

import androidx.compose.runtime.LaunchedEffect
import com.junkitout.app.data.Address
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.rememberCoroutineScope
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
    onOpenChat: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
) {
    var tab by remember { mutableIntStateOf(0) }
    val ctx = LocalContext.current
    StatusBarIcons(darkIcons = true)

    Scaffold(
        containerColor = JColors.Bg,
        bottomBar = {
            BottomBar(
                selected = tab,
                onSelect = {
                    if (it == 3) onOpenWallet() else tab = it
                },
                onBook = { onBook(null) }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeTab(area, city, onChangeLocation, onBook)
                1 -> OrdersTab(token, phone, startPayment)
                3 -> OrdersTab(token, phone, startPayment, walletMode = true)
                4 -> MenuTab(phone, name, token, onAddAddress, onNameChanged, onLogout, onOpenOrders = { tab = 1 }, onOpenWallet = onOpenWallet, onOpenChat = onOpenChat)
            }
        }
    }
}

// ───────────────────────── 11 Main Home ─────────────────────────
@Composable
private fun HomeTab(area: String, city: String, onChangeLocation: () -> Unit, onBook: (String?) -> Unit) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Header Location Bar & Notification Bell
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEAF6EC),
                modifier = Modifier.size(40.dp).clickable(onClick = onChangeLocation)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f).clickable(onClick = onChangeLocation)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Select location", fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 15.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(18.dp), tint = JColors.Text)
                }
                Text(city.ifBlank { "Bengaluru" }, color = JColors.Subtle, fontSize = 12.sp)
            }
            Box(Modifier.clickable { Toast.makeText(ctx, "No new notifications", Toast.LENGTH_SHORT).show() }) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, "Notifications", tint = JColors.Text, modifier = Modifier.size(22.dp))
                }
                Box(
                    Modifier.size(10.dp).clip(CircleShape).background(JColors.Red)
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Search Bar with Filter Sliders Icon
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().clickable { onBook(null) }
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, null, tint = JColors.Subtle, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Search for services...", color = JColors.Subtle, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Tune, "Filter", tint = JColors.Primary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hero Banner Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A3A1C),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF093318), Color(0xFF145C2F))))
                    .padding(18.dp)
            ) {
                Column(Modifier.fillMaxWidth(0.65f)) {
                    Text(
                        "Let's Recycle\nFor a Cleaner\nTomorrow",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Schedule a pickup and make your city greener.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF2E9A50),
                        modifier = Modifier.clickable { onBook(null) }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Book a Pickup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Text("♻️", fontSize = 72.sp, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }

        Spacer(Modifier.height(10.dp))

        // Carousel Indicator Dots
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 16.dp, height = 6.dp).clip(CircleShape).background(JColors.Primary))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD5EDD8)))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD5EDD8)))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD5EDD8)))
        }

        Spacer(Modifier.height(18.dp))

        // Our Services Section Header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Our Services", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = JColors.Text, modifier = Modifier.weight(1f))
            Text("View All", color = JColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBook(null) })
        }

        Spacer(Modifier.height(12.dp))

        // 2x2 Services Grid
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RichServiceCard(
                title = "E-Waste\nDisposal",
                subtitle = "Old electronics,\nIT equipment",
                iconText = "💻",
                cardBg = Color(0xFFEAF6EC),
                arrowBg = Color(0xFFD5EDD8),
                arrowTint = JColors.Primary,
                modifier = Modifier.weight(1f)
            ) { onBook("E-Waste Disposal") }

            RichServiceCard(
                title = "Household\nDry Waste",
                subtitle = "Paper, metal,\nglass, etc.",
                iconText = "🥫",
                cardBg = Color(0xFFFDF7EE),
                arrowBg = Color(0xFFFDE8D0),
                arrowTint = Color(0xFFE57A2E),
                modifier = Modifier.weight(1f)
            ) { onBook("Household Dry Waste") }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RichServiceCard(
                title = "Paper &\nCardboard",
                subtitle = "Newspapers,\ncartons, boxes",
                iconText = "📦",
                cardBg = Color(0xFFF7FAEE),
                arrowBg = Color(0xFFE8F5D8),
                arrowTint = Color(0xFF3DA35D),
                modifier = Modifier.weight(1f)
            ) { onBook("Paper & Cardboard") }

            RichServiceCard(
                title = "Plastic\nWaste",
                subtitle = "Bottles, containers,\npackaging",
                iconText = "🧴",
                cardBg = Color(0xFFEDF5FD),
                arrowBg = Color(0xFFD8E9F8),
                arrowTint = Color(0xFF2F80ED),
                modifier = Modifier.weight(1f)
            ) { onBook("Plastic Waste") }
        }

        Spacer(Modifier.height(18.dp))

        // 4 Value Proposition Items Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValuePropItem(iconText = "🍃", label = "Eco Friendly\nDisposal")
                Box(Modifier.width(1.dp).height(30.dp).background(JColors.Border))
                ValuePropItem(iconText = "🛡️", label = "Verified\nPickups")
                Box(Modifier.width(1.dp).height(30.dp).background(JColors.Border))
                ValuePropItem(iconText = "📅", label = "Flexible\nScheduling")
                Box(Modifier.width(1.dp).height(30.dp).background(JColors.Border))
                ValuePropItem(iconText = "💸", label = "Affordable\nPricing")
            }
        }

        Spacer(Modifier.height(18.dp))

        // Bottom Sustainability Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFEAF6EC),
            border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🌱", fontSize = 52.sp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cleaner Homes,\nGreener Bengaluru", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text, lineHeight = 19.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Dispose responsibly and help build a sustainable future.", fontSize = 12.sp, color = JColors.Subtle, lineHeight = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RichServiceCard(
    title: String,
    subtitle: String,
    iconText: String,
    cardBg: Color,
    arrowBg: Color,
    arrowTint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(iconText, fontSize = 36.sp, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(arrowBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = arrowTint, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text, lineHeight = 17.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 11.sp, color = JColors.Subtle, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun ValuePropItem(iconText: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEAF6EC)),
            contentAlignment = Alignment.Center
        ) {
            Text(iconText, fontSize = 18.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = JColors.Text,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
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

// ───────────────────────── 12 Edit Profile / App Menu ─────────────────────────
@Composable
private fun MenuTab(
    phone: String, name: String, token: String, onAddAddress: () -> Unit, onNameChanged: (String) -> Unit, onLogout: () -> Unit,
    onOpenOrders: () -> Unit, onOpenWallet: () -> Unit, onOpenChat: () -> Unit,
) {
    val shown = if (phone.length == 10) "+91 ${phone.take(5)} ${phone.drop(5)}" else "+91 $phone"
    val ctx = LocalContext.current
    var nameDraft by remember { mutableStateOf(name.ifBlank { "Junk-It-Out User" }) }
    var emailDraft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Header
        Text("Edit Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
        Text("Update your personal information", fontSize = 13.sp, color = JColors.Subtle)

        Spacer(Modifier.height(16.dp))

        // Top Avatar Card Banner
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFEAF6EC),
            border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFD5EDD8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = JColors.Primary, modifier = Modifier.size(48.dp))
                    }
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(JColors.Primary)
                            .clickable { Toast.makeText(ctx, "Photo upload coming soon", Toast.LENGTH_SHORT).show() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoCamera, "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(nameDraft.ifBlank { "Junk-It-Out User" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = JColors.Text)
                Spacer(Modifier.height(2.dp))
                Text("Tap the camera icon to change photo", fontSize = 12.sp, color = JColors.Subtle)
            }
        }

        Spacer(Modifier.height(18.dp))

        // Personal Details Section Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)

                Spacer(Modifier.height(12.dp))

                Text("Full Name", fontSize = 12.sp, color = JColors.Subtle)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = JColors.Subtle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JColors.Primary,
                        unfocusedBorderColor = JColors.Border,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("Mobile Number", fontSize = 12.sp, color = JColors.Subtle)
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF6F8F6),
                    border = BorderStroke(1.dp, JColors.Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🇮🇳", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(shown, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = JColors.Text, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEAF6EC)) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Lock, null, tint = JColors.Primary, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Verified", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JColors.Primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("ℹ️ Mobile number cannot be changed from here.", fontSize = 11.sp, color = JColors.Subtle)

                Spacer(Modifier.height(12.dp))

                Text("Email (Optional)", fontSize = 12.sp, color = JColors.Subtle)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = emailDraft,
                    onValueChange = { emailDraft = it },
                    placeholder = { Text("Enter your email address", fontSize = 14.sp, color = JColors.Subtle) },
                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = JColors.Subtle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JColors.Primary,
                        unfocusedBorderColor = JColors.Border,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Address Preview Section Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Address Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)

                Spacer(Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEAF6EC),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onAddAddress)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Manage your addresses from", fontSize = 11.sp, color = JColors.Subtle)
                            Text("My Addresses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Preferences Section Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)

                Spacer(Modifier.height(12.dp))

                // Notification Preferences
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAF8),
                    border = BorderStroke(1.dp, JColors.Border),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenChat() }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Notifications, null, tint = JColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JColors.Text)
                            Text("Manage what updates you receive", fontSize = 11.sp, color = JColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Language
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAF8),
                    border = BorderStroke(1.dp, JColors.Border),
                    modifier = Modifier.fillMaxWidth().clickable { Toast.makeText(ctx, "Language: English (India)", Toast.LENGTH_SHORT).show() }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Language, null, tint = JColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Language", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JColors.Text)
                            Text("English (India)", fontSize = 11.sp, color = JColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Save Changes Button
        PrimaryButton(if (busy) "Saving..." else "Save Changes", arrow = false, enabled = !busy) {
            busy = true
            scope.launch {
                when (val r = Api.updateName(nameDraft.trim(), token)) {
                    is Result.Ok -> {
                        busy = false
                        onNameChanged(r.value)
                        Toast.makeText(ctx, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Err -> {
                        busy = false
                        Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Logout Button
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFCE6E6))
                .clickable(onClick = onLogout).padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = JColors.Red)
            Spacer(Modifier.width(10.dp))
            Text("Logout", color = JColors.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(20.dp))
    }
}
