package com.statussaver.dele.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.statussaver.dele.services.NotificationService

class NotificationServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(action?.equals(Intent.ACTION_BOOT_COMPLETED) == true
                || action?.equals("Watomatic-RestartService-Broadcast") == true) {
            context?.let { restartService(context = context) }
        }
    }

    private fun restartService(context: Context) {
        val serviceIntent = Intent(context, NotificationService::class.java)
        try {
            context.startService(serviceIntent)
        } catch (e: IllegalStateException) {
            Log.e("NotifServiceRestart","Unable to restart notification service")
        }
    }
}