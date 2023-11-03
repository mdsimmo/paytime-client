package com.mdsimmo.paytime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import android.widget.Toast

class PingService : Service() {

    private var handler: Handler? = null

    override fun onCreate() {
        Log.i("Ping Service", "Service start requested")

        val channel2 = NotificationChannel("pinger","Ping Notification", NotificationManager.IMPORTANCE_LOW)
        channel2.description = "Displays ping active notification"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel2)


        val notificationBuilder = Notification.Builder(this, "pinger")
            .setOngoing(true)
            .setContentText("Do not restart phone!!!")
            .setContentTitle("Pinger")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory(Notification.CATEGORY_SERVICE)
        startForeground(23463562, notificationBuilder.build())

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            val looper = this.looper
            handler = Handler(looper)
        }

        handler?.post {
            while (true) {
                Log.d("HelloService", "Ping...")
                try {
                    Ponger.ping(this@PingService)
                    manager.notify(23463562, notificationBuilder
                        .setContentText("Last ping: " + Ponger.lastPingAttempt.toString())
                        .build()
                    )
                } catch (e: Exception) {
                    Log.e("HelloService", "Failed to ping", e)
                }
                try {
                    Thread.sleep(60000)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}