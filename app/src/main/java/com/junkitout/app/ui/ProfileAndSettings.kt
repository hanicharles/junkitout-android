package com.junkitout.app.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch

// ───────────────────────── 1. Edit Profile Screen ─────────────────────────
@Composable
fun EditProfileScreen(
    phone: String,
    name: String,
    email: String,
    photoUriStr: String,
    token: String,
    onBack: () -> Unit,
    onProfileUpdated: (newName: String, newEmail: String, newPhotoUri: String) -> Unit
) {
    val ctx = LocalContext.current
    val shownPhone = if (phone.length == 10) "+91 ${phone.take(5)} ${phone.drop(5)}" else "+91 $phone"

    var nameDraft by remember { mutableStateOf(name.ifBlank { "Junk-It-Out User" }) }
    var emailDraft by remember { mutableStateOf(email) }
    var photoUri by remember { mutableStateOf(photoUriStr) }
    var busy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
            Toast.makeText(ctx, "Profile photo updated", Toast.LENGTH_SHORT).show()
        }
    }

    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Update your name, email ID and photo", fontSize = 13.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Avatar Section Banner
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
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD5EDD8),
                            modifier = Modifier.size(88.dp)
                        ) {
                            if (photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, null, tint = JColors.Primary, modifier = Modifier.size(52.dp))
                                }
                            }
                        }

                        Box(
                            Modifier.size(30.dp).clip(CircleShape).background(JColors.Primary)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PhotoCamera, "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(nameDraft.ifBlank { "Junk-It-Out User" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = JColors.Text)
                    Spacer(Modifier.height(2.dp))
                    Text("Tap the camera icon to change profile photo", fontSize = 12.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Personal Info Form Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)

                    Spacer(Modifier.height(12.dp))

                    Text("Full Name *", fontSize = 12.sp, color = JColors.Subtle)
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
                            unfocusedBorderColor = JColors.Border
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Mobile Number (Read Only)", fontSize = 12.sp, color = JColors.Subtle)
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
                            Text(shownPhone, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = JColors.Text, modifier = Modifier.weight(1f))
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

                    Spacer(Modifier.height(12.dp))

                    Text("Email ID", fontSize = 12.sp, color = JColors.Subtle)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = emailDraft,
                        onValueChange = { emailDraft = it },
                        placeholder = { Text("Enter your email ID", fontSize = 14.sp, color = JColors.Subtle) },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = JColors.Subtle) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JColors.Primary,
                            unfocusedBorderColor = JColors.Border
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(if (busy) "Saving Changes..." else "Save Profile", arrow = false, enabled = nameDraft.isNotBlank() && !busy) {
                busy = true
                scope.launch {
                    when (val r = Api.updateName(nameDraft.trim(), token)) {
                        is Result.Ok -> {
                            busy = false
                            onProfileUpdated(nameDraft.trim(), emailDraft.trim(), photoUri)
                            Toast.makeText(ctx, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                        is Result.Err -> {
                            busy = false
                            Toast.makeText(ctx, r.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ───────────────────────── 2. App Settings Screen ─────────────────────────
@Composable
fun AppSettingsScreen(
    notificationsEnabled: Boolean,
    locationEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleLocation: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("App Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Configure your app preferences", fontSize = 13.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)

                    Spacer(Modifier.height(14.dp))

                    // Notifications Toggle Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEAF6EC)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Notifications, null, tint = JColors.Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Push Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                            Text(if (notificationsEnabled) "Notifications enabled" else "Notifications muted", fontSize = 11.sp, color = JColors.Subtle)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = JColors.Primary
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border.copy(alpha = 0.5f)))
                    Spacer(Modifier.height(12.dp))

                    // Location Toggle Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEDF5FD)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF2F80ED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("App Location Access", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JColors.Text)
                            Text(if (locationEnabled) "GPS location enabled" else "Location tracking disabled for app", fontSize = 11.sp, color = JColors.Subtle)
                        }
                        Switch(
                            checked = locationEnabled,
                            onCheckedChange = onToggleLocation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = JColors.Primary
                            )
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────── 3. About Us Screen ─────────────────────────
@Composable
fun AboutUsScreen(onBack: () -> Unit) {
    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("About Junk-It-Out", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Cleaner homes, greener tomorrow", fontSize = 13.sp, color = JColors.Subtle)
                }
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0A3A1C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("🌱", fontSize = 48.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Junk-It-Out", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                    Text("Bengaluru's Premier Recycling Initiative", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Who We Are", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Junk-It-Out is a technology-driven waste management platform headquartered in Bengaluru, Karnataka. We connect households, commercial offices, and IT parks with certified, eco-friendly doorstep waste pickup collectors.",
                        fontSize = 13.sp, color = JColors.Subtle, lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Our Mission", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Our mission is to achieve zero landfill waste by ensuring 100% responsible recycling for E-Waste, Paper & Cardboard, Plastic Waste, and Household Dry Scrap.",
                        fontSize = 13.sp, color = JColors.Subtle, lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Key Highlights", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(8.dp))
                    val stats = listOf(
                        "10,000+ Doorstep Pickups Completed",
                        "250+ Tonnes of Waste Recycled Responsibly",
                        "100% Background Verified Collection Executive Team",
                        "Instant Online UPI Payments & Discount Coupons"
                    )
                    stats.forEach { s ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = JColors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s, fontSize = 12.sp, color = JColors.Text)
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text("Contact & Headquarters", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(6.dp))
                    Text("🏢 Junk-It-Out Technologies Pvt Ltd", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = JColors.Text)
                    Text("📍 HSR Layout, Sector 1, Bengaluru, Karnataka 560102", fontSize = 12.sp, color = JColors.Subtle)
                    Text("✉️ support@junkitout.in", fontSize = 12.sp, color = JColors.Primary)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ───────────────────────── 4. Notification Center Dialog ─────────────────────────
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val isRead: Boolean
)

@Composable
fun NotificationCenterDialog(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onMarkAllRead) {
                    Text("Mark Read", fontSize = 12.sp, color = JColors.Primary)
                }
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().height(280.dp).verticalScroll(rememberScrollState())) {
                if (notifications.isEmpty()) {
                    Text("No notifications yet.", fontSize = 13.sp, color = JColors.Subtle, modifier = Modifier.padding(16.dp))
                } else {
                    notifications.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (item.isRead) Color(0xFFF6F8F6) else Color(0xFFEAF6EC),
                            border = BorderStroke(1.dp, if (item.isRead) JColors.Border else Color(0xFFD5EDD8)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JColors.Text, modifier = Modifier.weight(1f))
                                    Text(item.time, fontSize = 10.sp, color = JColors.Subtle)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.body, fontSize = 12.sp, color = JColors.Subtle, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
