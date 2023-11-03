package com.mdsimmo.paytime

import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseReceiver : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("Firebase", "Data: ${message.data}")
        Log.d("Firebase", "Priority: ${message.priority}")
        Log.d("Firebase", "Notification: ${message.notification}")

        Log.d("Firebase", "Sending ping")
        Ponger.ping(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Firebase", "New token: $token")
    }
}