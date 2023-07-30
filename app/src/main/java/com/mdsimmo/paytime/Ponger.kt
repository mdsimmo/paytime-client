package com.mdsimmo.paytime

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Ponger : FirebaseMessagingService() {

    companion object {
        fun ping(context: Context) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("Pinger", "Location access denied!")
                return
            }
            client.lastLocation.addOnSuccessListener { location: Location? ->
                val locationData = JSONObject()
                locationData.put("lon", location?.longitude)
                locationData.put("lat", location?.latitude)
                val request = JSONObject()
                request.put("location", locationData)
                request.put("client", "TODO")
                val requestString = request.toString()

                GlobalScope.launch {
                    val url = URL("https://nzvjuum1gh.execute-api.ap-southeast-2.amazonaws.com/ping-update")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 15*1000
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Content-Length", requestString.length.toString())
                    DataOutputStream(conn.getOutputStream()).use { it.writeBytes(requestString) }
                    BufferedReader(InputStreamReader(conn.inputStream)).use { bf ->
                        var line: String?
                        while (bf.readLine().also { line = it } != null) {
                            Log.i("Pinger", line ?: "null")
                        }
                    }
                    conn.disconnect()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.i("Ponger", "New token generated: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.i("Ponger", "Message Received" + message.data )
        Log.i("Ponger", "Message notification: " + message.notification?.body)

        Ponger.ping(this)
        Log.i("Ponger","Ping sent")
    }
}