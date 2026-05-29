package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subject = intent.getStringExtra("subject") ?: "Study"
        val message = intent.getStringExtra("message") ?: "Time to dive back into your goals and expand your mind!"
        
        Log.d("ReminderReceiver", "Alarm received for: $subject - $message")
        
        createNotificationChannel(context)
        showNotification(context, subject, message)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Study Reminders"
            val descriptionText = "Notifications to keep you focused and motivated"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, text: String) {
        // Safe check for Android 13 POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("ReminderReceiver", "POST_NOTIFICATIONS permission not granted. Skipping notification display.")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallResource()
            .setContentTitle("📚 Study Reminder: $title")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "SecurityException posting notification: ${e.message}")
        }
    }

    // Workaround helper to get resource safe icon without compile dependency block
    private fun NotificationCompat.Builder.setSmallResource(): NotificationCompat.Builder {
        this.setSmallIcon(android.R.drawable.ic_dialog_info)
        return this
    }

    companion object {
        const val CHANNEL_ID = "study_companion_reminders"
    }
}
