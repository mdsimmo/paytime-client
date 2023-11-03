package com.mdsimmo.paytime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity


class StartupService : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent?) {
        val intent = Intent(context, PingService::class.java)
        context.startForegroundService(intent)
        Log.i("Startup Service", "Service Boot")

        registerAlarm(context)
    }

    private fun registerAlarm(context: Context) {
        val i = Intent(context, PingService::class.java)
        val sender = PendingIntent.getBroadcast(context, 1245163, i, PendingIntent.FLAG_IMMUTABLE)

        // We want the alarm to go off 3 seconds from now.
        val firstTime: Long = SystemClock.elapsedRealtime() + (30 * 1000).toLong() //start 30 seconds after first register.

        // Schedule the alarm!
        val am = context.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 600000, sender) //10min interval
    }


}