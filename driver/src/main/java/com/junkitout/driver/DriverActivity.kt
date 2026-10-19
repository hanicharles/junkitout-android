package com.junkitout.driver

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DriverActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissions.toTypedArray())

        setContent {
            var token by remember { mutableStateOf("") }
            var driverInfo by remember { mutableStateOf<DriverInfo?>(null) }

            if (token.isBlank() || driverInfo == null) {
                DriverLoginScreen { t, d ->
                    token = t
                    driverInfo = d
                }
            } else {
                DriverDashboardScreen(
                    token = token,
                    driver = driverInfo!!,
                    onLogout = {
                        token = ""
                        driverInfo = null
                    }
                )
            }
        }
    }
}
