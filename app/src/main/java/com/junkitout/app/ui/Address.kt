package com.junkitout.app.ui

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import com.junkitout.app.data.Api
import com.junkitout.app.data.Place
import com.junkitout.app.data.Result
import com.junkitout.app.data.Address
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── 09 Add Address ─────────────────────────
@Composable
fun AddAddressScreen(place: Place?, token: String, onBack: () -> Unit, onSave: (Address) -> Unit) {
    var house by remember { mutableStateOf("") }
    var street by remember { mutableStateOf(place?.area.orEmpty()) }
    var landmark by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Home") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            BackButton(onBack)
            Spacer(Modifier.height(14.dp))
            Text("Add Your Address", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
            Text("Enter complete address for pickup", color = JColors.Subtle, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            AddressField("House / Flat / Building No.", house) { house = it }
            Spacer(Modifier.height(14.dp))
            AddressField("Street / Area", street) { street = it }
            Spacer(Modifier.height(14.dp))
            AddressField("Landmark (Optional)", landmark) { landmark = it }
            Spacer(Modifier.height(18.dp))
            Text("Address Type", fontSize = 12.sp, color = JColors.Subtle)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeChip(Icons.Filled.Home, "Home", type == "Home") { type = "Home" }
                TypeChip(Icons.Filled.Work, "Work", type == "Work") { type = "Work" }
                TypeChip(Icons.Filled.MoreHoriz, "Other", type == "Other") { type = "Other" }
            }
            Spacer(Modifier.height(28.dp))
            error?.let { Text(it, color = JColors.Red, fontSize = 13.sp); Spacer(Modifier.height(10.dp)) }
            PrimaryButton(if (busy) "Saving..." else "Save Address", arrow = false,
                enabled = house.isNotBlank() && street.isNotBlank() && !busy) {
                busy = true; error = null
                val addr = Address(house = house.trim(), street = street.trim(), landmark = landmark.trim(), type = type,
                    pincode = place?.pincode.orEmpty(), lat = place?.lat ?: 0.0, lng = place?.lng ?: 0.0)
                scope.launch {
                    when (val r = Api.saveAddress(addr, token)) {
                        is Result.Ok -> { busy = false; onSave(r.value) }
                        is Result.Err -> { busy = false; error = r.message }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = JColors.Subtle)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JColors.Primary,
                unfocusedBorderColor = JColors.Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            )
        )
    }
}

@Composable
private fun TypeChip(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFEAF6EC) else Color.White,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) JColors.Primary else JColors.Border),
        modifier = Modifier.size(width = 84.dp, height = 74.dp).clickable(onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (selected) JColors.Primary else JColors.Subtle, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = if (selected) JColors.Primary else JColors.Subtle)
        }
    }
}

// ───────────────────────── 10 Address Saved ─────────────────────────
@Composable
fun AddressSavedScreen(onContinue: () -> Unit) {
    ScreenRoot(dark = false, background = Brush.verticalGradient(listOf(Color.White, Color(0xFFE5F4E7)))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(80.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(Modifier.size(170.dp).clip(CircleShape).background(Color(0xFFD9EFDC)), contentAlignment = Alignment.Center) {
                    // Illustration placeholder – swap with your exported house artwork
                    Text("🏡", fontSize = 90.sp)
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(JColors.Primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Address Saved\nSuccessfully!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JColors.Text,
                textAlign = TextAlign.Center, lineHeight = 32.sp)
            Spacer(Modifier.height(10.dp))
            Text("Your address has been saved.\nYou can change it anytime\nfrom your profile.",
                color = JColors.Subtle, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(Modifier.weight(1f))
            PrimaryButton("Continue to Home", onClick = onContinue)
            Spacer(Modifier.height(24.dp))
        }
    }
}
