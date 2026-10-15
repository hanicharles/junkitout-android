package com.junkitout.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import com.junkitout.app.data.Api
import com.junkitout.app.data.Result

// ───────────────────────── 01 Splash ─────────────────────────
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(2200); onDone() }
    ScreenRoot(
        dark = true,
        background = Brush.verticalGradient(listOf(Color(0xFF0A3A1C), Color(0xFF0F5A2A), Color(0xFF0A3A1C)))
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(150.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(3.dp, Color(0xFFB7D8BE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Autorenew, null, tint = Color(0xFFCFEAD3), modifier = Modifier.size(90.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Junk-It-Out", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                "CLEANER HOMES\nGREENER TOMORROW",
                color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, letterSpacing = 2.sp,
                textAlign = TextAlign.Center, fontWeight = FontWeight.Medium, lineHeight = 18.sp
            )
        }
        Text(
            "Together for a\nCleaner, Greener Tomorrow",
            color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp)
        )
    }
}

// ───────────────────────── 02 Login ─────────────────────────
@Composable
fun LoginScreen(onContinue: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ScreenRoot(
        dark = false,
        background = Brush.verticalGradient(listOf(Color(0xFFE3F2E1), Color(0xFFA9D6B0), Color(0xFF3F9A5B)))
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(
                "A Cleaner\nTomorrow\nStarts Today",
                color = JColors.ForestGreen, fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Book pickup for your waste\nin just a few taps.",
                color = JColors.Text, fontSize = 14.sp, lineHeight = 20.sp
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Illustration placeholder – swap for your exported truck artwork (res/drawable)
                Text("🚛", fontSize = 110.sp)
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🇮🇳", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("+91", fontWeight = FontWeight.SemiBold, color = JColors.Text)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = phone,
                        onValueChange = { v -> phone = v.filter { it.isDigit() }.take(10) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        visualTransformation = VisualTransformation.None,
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (phone.isEmpty()) Text("Enter your phone number", color = JColors.Subtle, fontSize = 14.sp)
                            inner()
                        }
                    )
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = JColors.Subtle, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            error?.let {
                Text(it, color = JColors.Red, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
            PrimaryButton(if (busy) "Sending OTP..." else "Continue", enabled = phone.length == 10 && !busy) {
                busy = true; error = null
                scope.launch {
                    when (val r = Api.sendOtp(phone)) {
                        is Result.Ok -> { busy = false; onContinue(phone) }
                        is Result.Err -> { busy = false; error = r.message }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "By continuing, you agree to our\nTerms of Use & Privacy Policy",
                color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ───────────────────────── 03 OTP ─────────────────────────
@Composable
fun OtpScreen(phone: String, onBack: () -> Unit, onVerified: (token: String, name: String) -> Unit) {
    var otp by remember { mutableStateOf("") }
    var seconds by remember { mutableIntStateOf(30) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(seconds) { if (seconds > 0) { delay(1000); seconds-- } }
    LaunchedEffect(otp) {
        if (otp.length == 6) {
            busy = true; error = null
            when (val r = Api.verifyOtp(phone, otp)) {
                is Result.Ok -> { busy = false; onVerified(r.value.first, r.value.second) }
                is Result.Err -> { busy = false; error = r.message; otp = "" }
            }
        }
    }

    val shown = if (phone.length == 10) "+91 ${phone.take(5)} ${phone.drop(5)}" else "+91 $phone"

    ScreenRoot(dark = false, background = solid(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 12.dp)) {
                BackButton(onBack)
                Spacer(Modifier.height(16.dp))
                Text("Verify Your Number", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JColors.Text,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text("We have sent a 6-digit OTP to", fontSize = 13.sp, color = JColors.Subtle,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(shown, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                    Spacer(Modifier.width(12.dp))
                    Text("Edit", color = JColors.Primary, fontSize = 13.sp, textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(onClick = onBack))
                }
                Spacer(Modifier.height(28.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in 0 until 6) {
                        Box(
                            Modifier.weight(1f).height(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    if (i == otp.length) BorderStroke(2.dp, JColors.Primary) else BorderStroke(1.dp, JColors.Border),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(otp.getOrNull(i)?.toString() ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JColors.Text)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (busy) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JColors.Primary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                error?.let {
                    Text(it, color = JColors.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(16.dp))
                val label = if (seconds > 0) "Resend OTP in 00:%02d".format(seconds) else "Resend OTP"
                Text(
                    label,
                    color = if (seconds > 0) JColors.Text else JColors.Primary,
                    fontSize = 14.sp, fontWeight = if (seconds > 0) FontWeight.Normal else FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().clickable(enabled = seconds == 0 && !busy) {
                        seconds = 30; otp = ""; error = null
                        scope.launch { (Api.sendOtp(phone) as? Result.Err)?.let { error = it.message } }
                    },
                    textAlign = TextAlign.Center
                )
            }
            Keypad(
                onDigit = { d -> if (otp.length < 6) otp += d },
                onBackspace = { otp = otp.dropLast(1) }
            )
        }
    }
}

@Composable
private fun Keypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
    Column(
        Modifier.fillMaxWidth().background(Color(0xFFEEF4EE)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { r ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                r.forEach { k -> Key(k, Modifier.weight(1f)) { onDigit(k) } }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(Modifier.weight(1f))
            Key("0", Modifier.weight(1f)) { onDigit("0") }
            Box(
                Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.Backspace, "Delete", tint = JColors.Text) }
        }
    }
}

@Composable
private fun Key(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(52.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(label, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = JColors.Text) }
}
