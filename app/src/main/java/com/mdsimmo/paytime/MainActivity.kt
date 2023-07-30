package com.mdsimmo.paytime

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mdsimmo.paytime.ui.theme.PayTimeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        Log.d("MainActivity", "Permissions: $permissions")
        if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        lifecycleScope.launch {  }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("MainActivity", "Checking permissions")
            if (
                (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            ){
                Log.d("MainActivity", "All permissions granted")
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                ))
                requestPermissionLauncher.launch(arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ))
                requestPermissionLauncher.launch(arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ))
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PayTimeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(onScheduleRequest = {
                        // Declare the launcher at the top of your Activity/Fragment:
                        askNotificationPermission()

                        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                                return@OnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = task.result

                            // Log and toast
                            Log.i("MainActivity", "Token: $token")
                            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
                        })


                    }, onPingRequest = {
                        Ponger.ping(this)
                        Toast.makeText(this, "Ping sent", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

@Composable
fun App(onScheduleRequest: () -> Unit, onPingRequest: () -> Unit) {
    PayTimeTheme {
        Column {
            Button(onClick = onScheduleRequest) {
                Text(text = "Schedule")
            }
            Button(onClick = onPingRequest) {
                Text(text = "Ping")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    App({}, {})
}