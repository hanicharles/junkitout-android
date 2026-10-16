package com.junkitout.app.ui

import androidx.compose.runtime.LaunchedEffect
import com.junkitout.app.data.Address
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.material3.IconButton
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
    onOpenOffers: () -> Unit = {},
    onOpenTransactions: () -> Unit = {},
    onOpenEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
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
                    if (it == 3) onOpenTransactions() else tab = it
                },
                onBook = { onBook(null) }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeTab(area, city, onChangeLocation, onBook)
                1 -> OrdersTab(token, phone, startPayment, onOpenOffers = onOpenOffers)
                3 -> TransactionsScreen(token = token, onBack = { tab = 0 })
                4 -> MenuTab(
                    phone = phone, name = name, token = token,
                    onAddAddress = onAddAddress, onNameChanged = onNameChanged, onLogout = onLogout,
                    onOpenOrders = { tab = 1 }, onOpenTransactions = onOpenTransactions,
                    onOpenOffers = onOpenOffers, onOpenChat = onOpenChat,
                    onOpenEditProfile = onOpenEditProfile, onOpenSettings = onOpenSettings, onOpenAbout = onOpenAbout
                )
            }
        }
    }
}

// ───────────────────────── 11 Main Home ─────────────────────────
private data class HeroBannerData(
    val title: String,
    val subtitle: String,
    val gradStart: Color,
    val gradEnd: Color,
    val btnBg: Color,
    val btnText: String = "Book a Pickup"
)

private val HOME_BANNERS = listOf(
    HeroBannerData(
        title = "Small Steps,\nCleaner Streets",
        subtitle = "Schedule a pickup and keep our city beautiful.",
        gradStart = Color(0xFF0A3A1C),
        gradEnd = Color(0xFF145C2F),
        btnBg = Color(0xFFFFF3E0)
    ),
    HeroBannerData(
        title = "Give Waste\na Second Life",
        subtitle = "Schedule a pickup and turn waste into a cleaner future.",
        gradStart = Color(0xFF2E1052),
        gradEnd = Color(0xFF512DA8),
        btnBg = Color(0xFFEDE7F6)
    ),
    HeroBannerData(
        title = "A Greener City\nStarts Here",
        subtitle = "Schedule a pickup and make a positive impact today.",
        gradStart = Color(0xFF042D30),
        gradEnd = Color(0xFF00695C),
        btnBg = Color(0xFFE0F2F1)
    ),
    HeroBannerData(
        title = "Recycle Today,\nRenew Tomorrow",
        subtitle = "Schedule a pickup and help build a cleaner, greener city.",
        gradStart = Color(0xFF02322A),
        gradEnd = Color(0xFF004D40),
        btnBg = Color(0xFFCCFF90)
    )
)

@Composable
private fun HomeTab(area: String, city: String, onChangeLocation: () -> Unit, onBook: (String?) -> Unit) {
    val ctx = LocalContext.current
    var bannerIdx by remember { mutableIntStateOf(0) }
    var showNotifs by remember { mutableStateOf(false) }
    var hasUnreadNotifs by remember { mutableStateOf(true) }

    val notifications = remember {
        listOf(
            NotificationItem("1", "Booking Confirmed", "Your pickup for E-Waste Disposal has been scheduled.", "Just now", false),
            NotificationItem("2", "Payment Successful", "Payment of ₹60 verified for Order #JIO12345.", "2 hours ago", false),
            NotificationItem("3", "Address Saved", "New address 'Banahalli, Bengaluru' added.", "Yesterday", true),
            NotificationItem("4", "Welcome to Junk-It-Out", "Cleaner homes, greener tomorrow! Use coupon WELCOME10.", "3 days ago", true)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000L)
            bannerIdx = (bannerIdx + 1) % HOME_BANNERS.size
        }
    }

    if (showNotifs) {
        NotificationCenterDialog(
            notifications = notifications,
            onDismiss = { showNotifs = false },
            onMarkAllRead = {
                hasUnreadNotifs = false
                Toast.makeText(ctx, "All notifications marked as read", Toast.LENGTH_SHORT).show()
            }
        )
    }

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
            Box(Modifier.clickable { showNotifs = true }) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, "Notifications", tint = JColors.Text, modifier = Modifier.size(22.dp))
                }
                if (hasUnreadNotifs) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(JColors.Red)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hero Banner Carousel
        val activeBanner = HOME_BANNERS[bannerIdx]
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = activeBanner.gradStart,
            modifier = Modifier.fillMaxWidth().clickable {
                bannerIdx = (bannerIdx + 1) % HOME_BANNERS.size
            }
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(activeBanner.gradStart, activeBanner.gradEnd)))
                    .padding(18.dp)
            ) {
                Column(Modifier.fillMaxWidth(0.68f)) {
                    Text(
                        activeBanner.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        activeBanner.subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = activeBanner.btnBg,
                        modifier = Modifier.clickable { onBook(null) }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(activeBanner.btnText, color = Color(0xFF10251C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF10251C), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Text("♻️", fontSize = 72.sp, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }

        Spacer(Modifier.height(10.dp))

        // Carousel Indicator Dots
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            HOME_BANNERS.indices.forEach { idx ->
                val isActive = idx == bannerIdx
                Box(
                    Modifier.size(width = if (isActive) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (isActive) JColors.Primary else Color(0xFFD5EDD8))
                        .clickable { bannerIdx = idx }
                )
                if (idx < HOME_BANNERS.size - 1) Spacer(Modifier.width(4.dp))
            }
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RichServiceCard(
                    title = "E-Waste\nDisposal",
                    subtitle = "Old electronics,\nIT equipment",
                    iconText = "💻",
                    cardBg = Color(0xFFEAF6EC),
                    arrowBg = Color(0xFFD5EDD8),
                    arrowTint = JColors.Primary,
                    imageRes = com.junkitout.app.R.drawable.img_ewaste,
                    modifier = Modifier.weight(1f)
                ) { onBook("E-Waste Disposal") }

                RichServiceCard(
                    title = "Household\nDry Waste",
                    subtitle = "Paper, metal,\nglass, etc.",
                    iconText = "🥫",
                    cardBg = Color(0xFFFDF7EE),
                    arrowBg = Color(0xFFFDE8D0),
                    arrowTint = Color(0xFFE57A2E),
                    imageRes = com.junkitout.app.R.drawable.img_household_dry,
                    modifier = Modifier.weight(1f)
                ) { onBook("Household Dry Waste") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RichServiceCard(
                    title = "Paper &\nCardboard",
                    subtitle = "Newspapers,\ncartons, boxes",
                    iconText = "📦",
                    cardBg = Color(0xFFF4F7EE),
                    arrowBg = Color(0xFFE2EDCE),
                    arrowTint = Color(0xFF3DA35D),
                    imageRes = com.junkitout.app.R.drawable.img_paper_waste,
                    modifier = Modifier.weight(1f)
                ) { onBook("Paper & Cardboard") }

                RichServiceCard(
                    title = "Plastic\nWaste",
                    subtitle = "Bottles, containers,\npackaging",
                    iconText = "🧴",
                    cardBg = Color(0xFFEDF5FD),
                    arrowBg = Color(0xFFD0E3F7),
                    arrowTint = Color(0xFF2F80ED),
                    imageRes = com.junkitout.app.R.drawable.img_plastic_waste,
                    modifier = Modifier.weight(1f)
                ) { onBook("Plastic Waste") }
            }
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
    imageRes: Int? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.weight(1f))
                } else {
                    Text(iconText, fontSize = 36.sp, modifier = Modifier.weight(1f))
                }
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
                NavItem(Icons.Filled.AccountBalance, "Transactions", selected == 3) { onSelect(3) }
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
    onOpenOrders: () -> Unit, onOpenTransactions: () -> Unit, onOpenOffers: () -> Unit, onOpenChat: () -> Unit,
    onOpenEditProfile: () -> Unit = {}, onOpenSettings: () -> Unit = {}, onOpenAbout: () -> Unit = {}
) {
    val shown = if (phone.length == 10) "+91 ${phone.take(5)} ${phone.drop(5)}" else "+91 $phone"
    val ctx = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                Text("Manage your account and preferences", fontSize = 13.sp, color = JColors.Subtle)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, "Settings", tint = JColors.Text, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Profile User Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, JColors.Border),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenEditProfile)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFD5EDD8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = JColors.Primary, modifier = Modifier.size(32.dp))
                    }
                    Box(
                        Modifier.size(20.dp).clip(CircleShape).background(JColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(name.ifBlank { "Junk-It-Out User" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(2.dp))
                    Text(shown, fontSize = 13.sp, color = JColors.Subtle)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEAF6EC)) {
                        Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = JColors.Primary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Verified User", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JColors.Primary)
                        }
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(18.dp))

        // Profile Menu Items List
        val items = listOf(
            ProfileMenuItem(Icons.Filled.Edit, "Edit Profile", "Update your name, email and photo", onOpenEditProfile),
            ProfileMenuItem(Icons.Filled.LocationOn, "My Addresses", "Manage your saved addresses", onAddAddress),
            ProfileMenuItem(Icons.Filled.Receipt, "My Orders", "View and track your pickups", onOpenOrders),
            ProfileMenuItem(Icons.Filled.AccountBalance, "Transaction History", "View all payments and refunds", onOpenTransactions),
            ProfileMenuItem(Icons.Filled.LocalOffer, "Offers & Coupons", "View available offers and apply coupons", onOpenOffers),
            ProfileMenuItem(Icons.Filled.Help, "Support & Chatbot", "Get help or chat with us", onOpenChat),
            ProfileMenuItem(Icons.Filled.Info, "About Us", "Learn more about Junk-It-Out", onOpenAbout),
            ProfileMenuItem(Icons.Filled.Settings, "App Settings", "Notifications, language and more", onOpenSettings),
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, JColors.Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                items.forEachIndexed { idx, item ->
                    Row(
                        Modifier.fillMaxWidth().clickable(onClick = item.onClick).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                            Icon(item.icon, null, tint = JColors.Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                            Text(item.subtitle, fontSize = 11.sp, color = JColors.Subtle)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                    if (idx < items.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border.copy(alpha = 0.5f)))
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
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = JColors.Red, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Logout", color = JColors.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Sign out from your account", color = JColors.Red.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = JColors.Red, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)