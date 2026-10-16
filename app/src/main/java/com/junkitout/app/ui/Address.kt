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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.junkitout.app.data.Address
import com.junkitout.app.data.Api
import com.junkitout.app.data.Geo
import com.junkitout.app.data.Place
import com.junkitout.app.data.Result
import kotlinx.coroutines.launch

// ───────────────────────── Saved Address Management ─────────────────────────
@Composable
fun AddressManagerScreen(
    token: String,
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onUseLiveLocation: (Place) -> Unit = {},
    onSelectOnMap: () -> Unit = {},
    onEdit: (Address) -> Unit = {},
    onSelectAddress: ((Address) -> Unit)? = null
) {
    val ctx = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<Address?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var busyGps by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun detectGpsAndAdd() {
        busyGps = true
        scope.launch {
            val fix = Geo.currentFix(ctx)
            val p = if (fix != null) Geo.reverse(ctx, fix.latitude, fix.longitude)
                ?: Place("Current location", "Bengaluru", "", fix.latitude, fix.longitude)
            else Place("Bengaluru", "Bengaluru", "", 0.0, 0.0)

            val newAddress = Address(
                house = "Current Location",
                street = p.area.ifBlank { "Live GPS Area" },
                city = p.city.ifBlank { "Bengaluru" },
                pincode = p.pincode,
                type = "Home",
                isDefault = addresses.isEmpty(),
                lat = p.lat,
                lng = p.lng
            )

            when (val r = Api.saveAddress(newAddress, token)) {
                is Result.Ok -> {
                    busyGps = false
                    android.widget.Toast.makeText(ctx, "Current location saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    reload++
                }
                is Result.Err -> {
                    busyGps = false
                    android.widget.Toast.makeText(ctx, r.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(reload) {
        loading = true
        when (val r = Api.addresses(token)) {
            is Result.Ok -> { addresses = r.value; error = null }
            is Result.Err -> error = r.message
        }
        loading = false
    }

    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete Address") },
            text = { Text("Are you sure you want to delete this address (${target.house}, ${target.street})?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteCandidate = null
                    scope.launch {
                        Api.deleteAddress(target.id, token)
                        reload++
                    }
                }) { Text("Delete", color = JColors.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") }
            }
        )
    }

    ScreenRoot(dark = false, background = solid(Color(0xFFF4FAF6))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Saved Addresses", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Text("Manage your pickup locations", fontSize = 13.sp, color = JColors.Subtle)
                }
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEAF6EC)).clickable(onClick = onAddNew),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, "Add New Address", tint = JColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            when {
                loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JColors.Primary)
                }
                error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = JColors.Red, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("Retry", arrow = false) { reload++ }
                }
                addresses.isEmpty() -> {
                    // Empty State Illustration
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(160.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏡", fontSize = 80.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("No saved addresses yet", fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Add an address to easily schedule pickups\nat your preferred location.",
                            color = JColors.Subtle, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // 2 Action Cards
                    LocationActionCard(
                        title = "Use Current Location",
                        subtitle = if (busyGps) "Detecting location..." else "Get your live location using GPS",
                        icon = Icons.Filled.MyLocation,
                        iconBg = Color(0xFFEAF6EC),
                        iconTint = JColors.Primary,
                        cardBg = Color(0xFFEAF6EC),
                        cardBorder = null,
                        chevronTint = JColors.Primary,
                        onClick = ::detectGpsAndAdd
                    )

                    Spacer(Modifier.height(10.dp))

                    LocationActionCard(
                        title = "Enter Address Manually",
                        subtitle = "Type your address details",
                        icon = Icons.Filled.Edit,
                        iconBg = Color(0xFFFDF0E8),
                        iconTint = Color(0xFFE57A2E),
                        cardBg = Color.White,
                        cardBorder = BorderStroke(1.dp, Color(0xFFDCE7DD)),
                        chevronTint = JColors.Subtle,
                        onClick = onAddNew
                    )

                    Spacer(Modifier.height(20.dp))

                    // OR Divider
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(1.dp).background(JColors.Border))
                        Text("OR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JColors.Subtle, modifier = Modifier.padding(horizontal = 14.dp))
                        Box(Modifier.weight(1f).height(1.dp).background(JColors.Border))
                    }

                    Spacer(Modifier.height(20.dp))

                    // Why Save an Address Info Panel
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEAF6EC),
                        border = BorderStroke(1.dp, Color(0xFFD5EDD8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFD5EDD8)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Lightbulb, null, tint = JColors.Primary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text("Why save an address?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                            }

                            Spacer(Modifier.height(12.dp))

                            val benefits = listOf(
                                "Faster booking process",
                                "Use for multiple pickups",
                                "Manage Home, Work or Other addresses"
                            )
                            benefits.forEach { benefit ->
                                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = JColors.Primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(benefit, fontSize = 13.sp, color = JColors.Text)
                                }
                            }
                        }
                    }
                }
                else -> { // Saved addresses exist
                    Text("Your Saved Locations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JColors.Text)
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        addresses.forEach { a ->
                            AddressCard(
                                address = a,
                                onSelect = { onSelectAddress?.invoke(a) },
                                onSetDefault = {
                                    scope.launch {
                                        Api.setDefaultAddress(a.id, token)
                                        reload++
                                    }
                                },
                                onEdit = { onEdit(a) },
                                onDelete = { deleteCandidate = a }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 2 Action Cards below saved list
                    LocationActionCard(
                        title = "Use Current Location",
                        subtitle = if (busyGps) "Detecting location..." else "Get your live location using GPS",
                        icon = Icons.Filled.MyLocation,
                        iconBg = Color(0xFFEAF6EC),
                        iconTint = JColors.Primary,
                        cardBg = Color(0xFFEAF6EC),
                        cardBorder = null,
                        chevronTint = JColors.Primary,
                        onClick = ::detectGpsAndAdd
                    )

                    Spacer(Modifier.height(10.dp))

                    LocationActionCard(
                        title = "Enter Address Manually",
                        subtitle = "Type your address details",
                        icon = Icons.Filled.Edit,
                        iconBg = Color(0xFFFDF0E8),
                        iconTint = Color(0xFFE57A2E),
                        cardBg = Color.White,
                        cardBorder = BorderStroke(1.dp, Color(0xFFDCE7DD)),
                        chevronTint = JColors.Subtle,
                        onClick = onAddNew
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LocationActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    cardBg: Color,
    cardBorder: BorderStroke?,
    chevronTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = cardBorder,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JColors.Text)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = JColors.Subtle)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null, tint = chevronTint, modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onSelect: () -> Unit,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (address.isDefault) Color(0xFFEAF6EC) else Color.White,
        border = BorderStroke(if (address.isDefault) 1.5.dp else 1.dp, if (address.isDefault) JColors.Primary else JColors.Border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (address.type) {
                    "Work" -> Icons.Filled.Domain
                    "Other" -> Icons.Filled.LocationOn
                    else -> Icons.Filled.Home
                }
                val iconBg = when (address.type) {
                    "Work" -> Color(0xFFE8F1FD)
                    "Other" -> Color(0xFFFDF0E8)
                    else -> Color(0xFFEAF6EC)
                }
                val iconTint = when (address.type) {
                    "Work" -> Color(0xFF2F80ED)
                    "Other" -> Color(0xFFE57A2E)
                    else -> JColors.Primary
                }

                Box(Modifier.size(38.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(address.type, fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 16.sp)

                if (address.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD5EDD8)) {
                        Text("Default", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JColors.Primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(Modifier.weight(1f))

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.MoreVert, "More Options", tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (!address.isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as Default", fontSize = 13.sp) },
                                onClick = { showMenu = false; onSetDefault() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Edit Address", fontSize = 13.sp) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Address", fontSize = 13.sp, color = JColors.Red) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("${address.house}, ${address.street}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = JColors.Text)
            val line2 = listOf(address.landmark, address.city, address.pincode).filter { it.isNotBlank() }.joinToString(", ")
            if (line2.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(line2, fontSize = 12.sp, color = JColors.Subtle)
            }

            if (address.name.isNotBlank() || address.phone.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(listOf(address.name, address.phone).filter { it.isNotBlank() }.joinToString(" • "), fontSize = 12.sp, color = JColors.Subtle)
            }
        }
    }
}

// ───────────────────────── 09 Add/Edit Address ─────────────────────────
@Composable
fun AddAddressScreen(
    place: Place?,
    token: String,
    editingAddress: Address? = null,
    onBack: () -> Unit,
    onSave: (Address) -> Unit
) {
    var name by remember { mutableStateOf(editingAddress?.name.orEmpty()) }
    var phone by remember { mutableStateOf(editingAddress?.phone.orEmpty()) }
    var house by remember { mutableStateOf(editingAddress?.house ?: "") }
    var street by remember { mutableStateOf(editingAddress?.street ?: place?.area.orEmpty()) }
    var landmark by remember { mutableStateOf(editingAddress?.landmark.orEmpty()) }
    var city by remember { mutableStateOf(editingAddress?.city ?: place?.city.orEmpty().ifBlank { "Bengaluru" }) }
    var pincode by remember { mutableStateOf(editingAddress?.pincode ?: place?.pincode.orEmpty()) }
    var type by remember { mutableStateOf(editingAddress?.type ?: "Home") }
    var isDefault by remember { mutableStateOf(editingAddress?.isDefault ?: false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            BackButton(onBack)
            Spacer(Modifier.height(14.dp))
            Text(if (editingAddress != null) "Edit Address" else "Add Your Address", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
            Text("Enter complete details for smooth pickup", color = JColors.Subtle, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            AddressField("Recipient Name (Optional)", name) { name = it }
            Spacer(Modifier.height(12.dp))
            AddressField("Contact Mobile Number (Optional)", phone) { phone = it }
            Spacer(Modifier.height(12.dp))
            AddressField("House / Flat / Building No. *", house) { house = it }
            Spacer(Modifier.height(12.dp))
            AddressField("Street / Locality / Area *", street) { street = it }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { AddressField("City", city) { city = it } }
                Box(Modifier.weight(1f)) { AddressField("PIN Code", pincode) { pincode = it } }
            }
            Spacer(Modifier.height(12.dp))
            AddressField("Landmark (Optional)", landmark) { landmark = it }

            Spacer(Modifier.height(16.dp))
            Text("Address Type", fontSize = 12.sp, color = JColors.Subtle)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeChip(Icons.Filled.Home, "Home", type == "Home") { type = "Home" }
                TypeChip(Icons.Filled.Work, "Work", type == "Work") { type = "Work" }
                TypeChip(Icons.Filled.MoreHoriz, "Other", type == "Other") { type = "Other" }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isDefault = !isDefault }) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it }, colors = CheckboxDefaults.colors(checkedColor = JColors.Primary))
                Text("Set as Default Address", fontSize = 14.sp, color = JColors.Text)
            }

            Spacer(Modifier.height(24.dp))
            error?.let { Text(it, color = JColors.Red, fontSize = 13.sp); Spacer(Modifier.height(10.dp)) }
            PrimaryButton(if (busy) "Saving..." else "Save Address", arrow = false,
                enabled = house.isNotBlank() && street.isNotBlank() && !busy) {
                busy = true; error = null
                val addr = Address(
                    id = editingAddress?.id.orEmpty(),
                    name = name.trim(),
                    phone = phone.trim(),
                    house = house.trim(),
                    street = street.trim(),
                    landmark = landmark.trim(),
                    city = city.trim().ifBlank { "Bengaluru" },
                    pincode = pincode.trim(),
                    type = type,
                    isDefault = isDefault,
                    lat = editingAddress?.lat ?: place?.lat ?: 0.0,
                    lng = editingAddress?.lng ?: place?.lng ?: 0.0
                )
                scope.launch {
                    val result = if (editingAddress != null) Api.updateAddress(addr, token) else Api.saveAddress(addr, token)
                    when (result) {
                        is Result.Ok -> { busy = false; onSave(result.value) }
                        is Result.Err -> { busy = false; error = result.message }
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
            Text("Your address has been saved.\nYou can change or manage it anytime\nfrom your profile.",
                color = JColors.Subtle, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(Modifier.weight(1f))
            PrimaryButton("Continue to Home", onClick = onContinue)
            Spacer(Modifier.height(24.dp))
        }
    }
}
