package com.junkitout.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.junkitout.app.data.Place
import com.junkitout.app.data.Session

@Composable
fun AppNav(startPayment: (orderId: String, amountPaise: Long, phone: String) -> Unit) {
    val ctx = LocalContext.current
    val session = remember { Session(ctx) }
    val nav = rememberNavController()
    var phone by remember { mutableStateOf(session.phone) }
    var name by remember { mutableStateOf(session.name) }
    var token by remember { mutableStateOf(session.token) }
    var place by remember {
        mutableStateOf(
            if (session.area.isNotBlank()) Place(session.area, "Bengaluru", session.pincode, session.lat, session.lng) else null
        )
    }
    var bookService by remember { mutableStateOf<String?>(null) }

    fun goHome() = nav.navigate("home") { popUpTo(0) { inclusive = true } }

    NavHost(navController = nav, startDestination = "splash") {

        composable("splash") {
            SplashScreen {
                val dest = if (!session.loggedIn) "login" else "home"
                nav.navigate(dest) { popUpTo("splash") { inclusive = true } }
            }
        }
        composable("login") {
            LoginScreen { p -> phone = p; nav.navigate("otp") }
        }
        composable("otp") {
            OtpScreen(phone, onBack = { nav.popBackStack() }, onVerified = { t, n ->
                token = t; name = n
                session.token = t; session.name = n; session.phone = phone
                nav.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("location") {
            LocationScreen(
                onSkip = { goHome() },
                onPlace = { p -> place = p; nav.navigate("checking") },
                onManual = { nav.navigate("address") }
            )
        }
        composable("checking") {
            val p = place
            if (p == null) nav.popBackStack() else
                CheckingScreen(p, token) { ok ->
                    nav.navigate(if (ok) "available" else "unavailable") { popUpTo("checking") { inclusive = true } }
                }
        }
        composable("available") {
            place?.let { p ->
                ServiceAvailableScreen(p, onChange = { nav.navigate("location") }, onContinue = { nav.navigate("confirm") })
            }
        }
        composable("unavailable") {
            ServiceUnavailableScreen(
                onSearchNearby = { nav.navigate("location") },
                onChange = { nav.navigate("location") }
            )
        }
        composable("confirm") {
            place?.let { p ->
                ConfirmLocationScreen(p, onBack = { nav.popBackStack() }, onConfirm = {
                    session.area = p.area; session.pincode = p.pincode; session.lat = p.lat; session.lng = p.lng
                    nav.navigate("address")
                })
            }
        }
        composable("address") {
            AddAddressScreen(place, token, onBack = { nav.popBackStack() }, onSave = { nav.navigate("saved") })
        }
        composable("addresses") {
            AddressManagerScreen(
                token = token,
                onBack = { nav.popBackStack() },
                onAddNew = { nav.navigate("address") },
                onEdit = { nav.navigate("address") }
            )
        }
        composable("saved") {
            AddressSavedScreen { goHome() }
        }
        composable("home") {
            MainScreen(
                area = place?.area ?: "Select location", city = place?.city ?: "Bengaluru",
                phone = phone, name = name, token = token,
                startPayment = startPayment,
                onBook = { svc -> bookService = svc; nav.navigate("book") },
                onChangeLocation = { nav.navigate("addresses") },
                onAddAddress = { nav.navigate("addresses") },
                onNameChanged = { n -> name = n; session.name = n },
                onLogout = {
                    session.clear(); token = ""; name = ""; phone = ""; place = null
                    nav.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onOpenChat = { nav.navigate("chat") }
            )
        }
        composable("book") {
            BookingScreen(
                initialService = bookService, token = token, phone = phone, area = place?.area ?: "your address",
                startPayment = startPayment,
                onBack = { nav.popBackStack() },
                onDone = { nav.popBackStack() }
            )
        }
        composable("chat") {
            SupportChatScreen(onBack = { nav.popBackStack() })
        }
    }
}
