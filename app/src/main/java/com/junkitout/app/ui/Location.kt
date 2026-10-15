package com.junkitout.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.junkitout.app.BuildConfig
import com.junkitout.app.data.Api
import com.junkitout.app.data.Geo
import com.junkitout.app.data.Place
import com.junkitout.app.data.Result

@Composable
private fun MapBlock(modifier: Modifier = Modifier, bigPin: Boolean = true, userDot: Boolean = false) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val line = Color(0xFFCFE3D2)
            val step = 38.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(line, Offset(x, 0f), Offset(x, size.height), 2f); x += step }
            var y = 0f
            while (y < size.height) { drawLine(line, Offset(0f, y), Offset(size.width, y), 2f); y += step }
            drawLine(Color.White, Offset(0f, size.height * .7f), Offset(size.width, size.height * .3f), 14f)
            drawLine(Color.White, Offset(size.width * .25f, 0f), Offset(size.width * .6f, size.height), 12f)
        }
        Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF3FA55F), modifier = Modifier.offset((-90).dp, (-40).dp).size(26.dp))
        Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF3FA55F), modifier = Modifier.offset(80.dp, (-60).dp).size(22.dp))
        Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF3FA55F), modifier = Modifier.offset((-60).dp, 50.dp).size(22.dp))
        if (userDot) Box(Modifier.offset(y = 30.dp).size(18.dp).clip(CircleShape).background(Color(0xFF2F80ED)).border(3.dp, Color.White, CircleShape))
        if (bigPin) Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.offset(y = (-6).dp).size(64.dp))
    }
}

// ───────────────────────── 04 Location Selection ─────────────────────────
@Composable
fun LocationScreen(onSkip: () -> Unit, onPlace: (Place) -> Unit, onManual: () -> Unit) {
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    fun detect() {
        busy = true; error = null
        scope.launch {
            val fix = Geo.currentFix(ctx)
            if (fix == null) { busy = false; error = "Couldn't get your location. Turn on GPS or use Search."; return@launch }
            val place = Geo.reverse(ctx, fix.latitude, fix.longitude)
                ?: Place("Current location", "Bengaluru", "", fix.latitude, fix.longitude)
            busy = false; onPlace(place)
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) detect() else error = "Location permission denied. Use Search or Enter Manually."
    }
    fun onCurrentClick() {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) detect()
        else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    fun doSearch() {
        val q = query.trim()
        if (q.isEmpty()) { error = "Enter an area or pincode"; return }
        busy = true; error = null
        scope.launch {
            val place = Geo.search(ctx, q)
            busy = false
            if (place == null) error = "Couldn't find \"$q\". Try another area or a 6-digit pincode."
            else onPlace(place)
        }
    }

    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFEAF5EA))) {
                MapBlock(Modifier.fillMaxSize())
                Text("Skip", color = JColors.Text, fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).clickable(onClick = onSkip))
            }
            Spacer(Modifier.height(20.dp))
            Text("Choose\nYour Location", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JColors.Text, lineHeight = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("We need your location to check\nservice availability", color = JColors.Subtle, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            LocationOption(Icons.Filled.MyLocation, "Use Current Location", "Detect my current location", true) { if (!busy) onCurrentClick() }
            Spacer(Modifier.height(12.dp))
            LocationOption(Icons.Filled.Search, "Search Location", "Search by area or pincode", false) { showSearch = true }
            Spacer(Modifier.height(12.dp))
            LocationOption(Icons.Filled.Edit, "Enter Manually", "Enter your address manually", false, onManual)
            Spacer(Modifier.height(14.dp))
            if (busy) Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Finding your location...", color = JColors.Subtle, fontSize = 13.sp)
            }
            error?.let { Text(it, color = JColors.Red, fontSize = 13.sp) }
        }
    }

    if (showSearch) {
        AlertDialog(
            onDismissRequest = { showSearch = false },
            title = { Text("Search Location") },
            text = {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    placeholder = { Text("Area or pincode") }
                )
            },
            confirmButton = {
                TextButton(onClick = { showSearch = false; doSearch() }) { Text("Search") }
            },
            dismissButton = { TextButton(onClick = { showSearch = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun LocationOption(icon: ImageVector, title: String, sub: String, highlighted: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (highlighted) Color(0xFFEAF6EC) else Color.White,
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) JColors.Primary else JColors.Border),
        shadowElevation = if (highlighted) 0.dp else 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(JColors.Light),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = JColors.Primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = JColors.Text, fontSize = 15.sp)
                Text(sub, color = JColors.Subtle, fontSize = 12.sp)
            }
            if (highlighted) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(JColors.Primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ───────────────────────── 05 Checking Availability ─────────────────────────
@Composable
fun CheckingScreen(place: Place, token: String, onDone: (Boolean) -> Unit) {
    var done by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        error = null; done = 0
        delay(500); done = 1
        val result = Api.isServiceable(place, token)
        delay(500); done = 2
        delay(500); done = 3
        when (result) {
            is Result.Ok -> onDone(result.value)
            is Result.Err -> error = result.message
        }
    }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "scale"
    )
    ScreenRoot(dark = false, background = Brush.verticalGradient(listOf(Color.White, Color(0xFFE9F5EA)))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(36.dp))
            Text("Checking\nService Availability", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = JColors.Text, textAlign = TextAlign.Center, lineHeight = 30.sp)
            Spacer(Modifier.height(10.dp))
            Text("Please wait while we check if we\nserve your area...", color = JColors.Subtle, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Box(Modifier.size(220.dp).scale(pulse), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().clip(CircleShape).background(JColors.Mid.copy(alpha = 0.10f)))
                Box(Modifier.size(150.dp).clip(CircleShape).background(JColors.Mid.copy(alpha = 0.18f)))
                Box(Modifier.size(90.dp).clip(CircleShape).background(Color.White).border(2.dp, JColors.Mint, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary, modifier = Modifier.size(52.dp))
                }
            }
            Spacer(Modifier.height(40.dp))
            val steps = listOf("Getting your location", "Checking service area", "Verifying availability")
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                steps.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(if (i < done) JColors.Primary else JColors.Border),
                            contentAlignment = Alignment.Center
                        ) { if (i < done) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(15.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Text(s, color = JColors.Text, fontSize = 14.sp)
                    }
                }
            }
            error?.let {
                Spacer(Modifier.height(20.dp))
                Text(it, color = JColors.Red, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                PrimaryButton("Retry", arrow = false) { attempt++ }
            }
        }
    }
}

// ───────────────────────── 06 Service Available ─────────────────────────
@Composable
fun ServiceAvailableScreen(place: Place, onChange: () -> Unit, onContinue: () -> Unit) {
    ScreenRoot(dark = false, background = Brush.verticalGradient(listOf(Color.White, Color(0xFFE3F3E5)))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Box(contentAlignment = Alignment.Center) {
                Text("•  ✦  •", color = Color(0xFFE8A33D), fontSize = 14.sp, modifier = Modifier.offset(y = (-58).dp))
                Box(Modifier.size(96.dp).clip(CircleShape).background(JColors.Primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Great News!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
            Spacer(Modifier.height(8.dp))
            Text("We provide our services\nin your area", color = JColors.Subtle, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(JColors.Light), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(place.area, fontWeight = FontWeight.Bold, color = JColors.Text, fontSize = 16.sp)
                            Text(listOf(place.city, place.pincode).filter { it.isNotBlank() }.joinToString(" - "), color = JColors.Subtle, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JColors.Border))
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onChange), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, null, tint = JColors.Primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change Location", color = JColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Continue", onClick = onContinue)
        }
    }
}

// ───────────────────────── 07 Service Not Available ─────────────────────────
@Composable
fun ServiceUnavailableScreen(onSearchNearby: () -> Unit, onChange: () -> Unit) {
    ScreenRoot(
        dark = true,
        background = Brush.verticalGradient(listOf(Color(0xFF0B3D1E), Color(0xFF0F5A2A), Color(0xFF0A3A1C)))
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(70.dp))
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.LocationOn, null, tint = JColors.Red, modifier = Modifier.size(110.dp))
                Box(
                    Modifier.offset(x = 0.dp, y = 6.dp).size(34.dp).clip(CircleShape).background(Color.White).border(3.dp, JColors.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Close, null, tint = JColors.Red, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.height(22.dp))
            Text("Oops!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Currently we do not provide\nservices in this area.", color = Color.White.copy(alpha = .9f),
                fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(26.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF8A5A3C).copy(alpha = 0.85f)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Text("Try a different location\nor search nearby areas.", color = Color.White, fontSize = 13.sp)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Search Nearby Areas", arrow = false, onClick = onSearchNearby)
            Spacer(Modifier.height(12.dp))
            OutlineButton("Change Location", onClick = onChange)
        }
    }
}

// ───────────────────────── 08 Confirm Location ─────────────────────────
@Composable
fun ConfirmLocationScreen(place: Place, onBack: () -> Unit, onConfirm: () -> Unit) {
    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
            }
            Text("Confirm Your Location", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JColors.Text,
                modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFEAF2EA))) {
                if (BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank() && (place.lat != 0.0 || place.lng != 0.0)) {
                    val pos = LatLng(place.lat, place.lng)
                    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(pos, 16f) }
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camera) {
                        Marker(state = MarkerState(pos), title = place.area)
                    }
                } else {
                    // No Maps key configured: fall back to the drawn placeholder instead of a blank grey map.
                    MapBlock(Modifier.fillMaxSize(), bigPin = true, userDot = true)
                }
            }
            Column(Modifier.padding(20.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(JColors.Light), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = JColors.Primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(place.area, fontWeight = FontWeight.Bold, color = JColors.Text)
                            Text(listOf(place.city, place.pincode).filter { it.isNotBlank() }.joinToString(" - "), color = JColors.Subtle, fontSize = 13.sp)
                            if (place.lat != 0.0 || place.lng != 0.0)
                                Text("%.4f, %.4f".format(place.lat, place.lng), color = JColors.Subtle, fontSize = 11.sp)
                        }
                        Row(Modifier.clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Edit, null, tint = JColors.Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", color = JColors.Primary, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Confirm Location", onClick = onConfirm)
            }
        }
    }
}
