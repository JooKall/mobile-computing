package com.example.hw4sensorsandnotifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun showNotification(context: Context, message: String){

    // Create an explicit intent for an Activity in your app.
    val intent = Intent(context, MainActivity::class.java).apply {

        // FLAG_ACTIVITY_CLEAR_TOP == all activities above are destroyed, and the existing activity is brought to the foreground
        // FLAG_ACTIVITY_SINGLE_TOP == If it is at the top, it reuses the existing instance and if it’s not at the top, behaves like a normal launch.
        // FLAG_ACTIVITY_NEW_TASK = if app is in the foreground, creates a new instance of the activity and if background, ensures the activity can start when not visible
        // FLAG_ACTIVITY_CLEAR_TASK = Clears all existing activities in the task before starting the new activity, i.e. shows only the activity starting
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Notification")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent) // Set the intent that fires when the user taps the notification.
        .setAutoCancel(true)

    NotificationManagerCompat.from(context)
        .notify(NOTIFICATION_ID, builder.build())
}