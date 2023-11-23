package com.mdsimmo.paytime

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.util.JsonReader
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime


class Ponger() {

    companion object {

        val lastPingAttempt = MutableLiveData(LocalDateTime.now().minusYears(10))
        val lastPingSuccess =  MutableLiveData(LocalDateTime.now().minusYears(10))

        fun internet_connection(ctx: Context): Boolean {
            //Check if connected to internet, output accordingly
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

        @SuppressLint("MissingPermission")
        fun ping(context: Context) {
            lastPingAttempt.postValue(LocalDateTime.now())
            if (!internet_connection(context)) {
                Log.i("Ponger", "No internet")
                return
            }

            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation.addOnSuccessListener { location: Location? ->
                val locationData = JSONObject()
                locationData.put("lon", location?.longitude)
                locationData.put("lat", location?.latitude)
                val request = JSONObject()
                request.put("location", locationData)
                request.put("client", "TODO")
                var requestString = request.toString()
                Log.i("Ponger", requestString)

                GlobalScope.launch {
                    try {
                        val fail = checkFailed()
                        if (fail) {
                            requestString = requestString.replace("{", "{\"fail\":true,")
                            Log.i("Ponger", requestString)
                        }
                        val url = URL("https://nzvjuum1gh.execute-api.ap-southeast-2.amazonaws.com/ping-update")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 15 * 1000
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
                        lastPingSuccess.postValue(LocalDateTime.now())
                    } catch (e: Exception) {
                        Log.e("Ponger", "Network issue", e)
                    }
                }
            }
        }

        private fun checkFailed(): Boolean {
            return try {
                val connection = URL("https://reddit.com").openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.connect()
                connection.disconnect()
                Log.w("Ponger", "Can read reddit")
                true
            } catch (e: Exception) {
                Log.i("Ponger", "Failed to read reddit", e)
                false
            }
        }
    }


}