package com.mdsimmo.paytime

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mdsimmo.paytime.ui.theme.PayTimeTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


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
    private val lastPingTextState = mutableStateOf(Ponger.lastPingAttempt)

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

                        startForegroundService(Intent(this, PingService::class.java))

                    }, onPingRequest = {
                        Ponger.ping(this)
                        Toast.makeText(this, "Ping sent", Toast.LENGTH_SHORT).show()
                    }, getPermission = {
                        if (ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
                        }
                    }, getToken = {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("MainActivity", "FCM token registration failed", task.exception)
                                    return@OnCompleteListener
                                }

                                val token = task.result
                                Log.w("MainActivity", "Token: $token")
                                Toast.makeText(this@MainActivity, "Token: $token", Toast.LENGTH_SHORT).show()
                            })
                    }, resetTimer = {
                        GlobalScope.launch {
                            try {
                                Log.i(
                                    "Reset Result",
                                    URL("https://nzvjuum1gh.execute-api.ap-southeast-2.amazonaws.com/ping-reset").readText()
                                )
                            } catch (e: Exception) {
                                Log.e("Main Activity", "Failed to fetch", e)
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun App(onScheduleRequest: () -> Unit, onPingRequest: () -> Unit, getPermission: () -> Unit,
        getToken: () -> Unit, resetTimer: () -> Unit) {
    PayTimeTheme {
        Column {
            Button(onClick = onScheduleRequest) {
                Text(text = "Schedule")
            }
            Button(onClick = onPingRequest) {
                Text(text = "Ping")
            }
            Button(onClick = getPermission) {
                Text(text = "Get Permissions")
            }
            Button(onClick = getToken) {
                Text(text = "Get Token")
            }
            Button(onClick = resetTimer) {
                Text(text = "Reset Time")
            }
            val lastPingAttempt: LocalDateTime by Ponger.lastPingAttempt.observeAsState(LocalDateTime.now().minusYears(20))
            BasicText(text = "last attempt: $lastPingAttempt", Modifier.background(Color.White))
            val lastPingSuccess: LocalDateTime by Ponger.lastPingSuccess.observeAsState(LocalDateTime.now().minusYears(20))
            BasicText(text = "last success: $lastPingSuccess", Modifier.background(Color.White))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    App({}, {}, {}, {}, {})
}