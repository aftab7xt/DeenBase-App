package com.deenbase.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationHelper {

    const val QURAN_CHANNEL_ID = "quran_reminder"
    const val DHIKR_CHANNEL_ID = "dhikr_reminder"
    const val ADHKAR_CHANNEL_ID = "adhkar_reminder"

    const val QURAN_NOTIF_ID_1 = 1001
    const val QURAN_NOTIF_ID_2 = 1002
    const val DHIKR_NOTIF_ID_1 = 2001
    const val DHIKR_NOTIF_ID_2 = 2002

    const val REQUEST_QURAN_1 = 101
    const val REQUEST_QURAN_2 = 102
    const val REQUEST_DHIKR_1 = 201
    const val REQUEST_DHIKR_2 = 202
    const val REQUEST_MORNING_ADHKAR = 301
    const val REQUEST_EVENING_ADHKAR = 401

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(QURAN_CHANNEL_ID, "Quran Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Daily reminder to read Quran"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(DHIKR_CHANNEL_ID, "Dhikr Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Daily reminder for Subhanallahi wa bihamdihi"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(ADHKAR_CHANNEL_ID, "Morning & Evening Adhkar", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Daily morning and evening adhkar reminders"
            }
        )
    }

    fun scheduleAdhkarAlarms(context: Context) {
        scheduleAlarm(context, 5, 0, REQUEST_MORNING_ADHKAR, MorningAdhkarReceiver::class.java)
        scheduleAlarm(context, 18, 0, REQUEST_EVENING_ADHKAR, EveningAdhkarReceiver::class.java)
    }

    fun scheduleAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        requestCode: Int,
        receiverClass: Class<*>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, receiverClass).apply {
            putExtra("notif_id", requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, requestCode: Int, receiverClass: Class<*>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context, prefs: NotificationPrefs) {
        if (prefs.quranEnabled) {
            scheduleAlarm(context, prefs.quranHour1, prefs.quranMinute1, REQUEST_QURAN_1, QuranNotificationReceiver::class.java)
        } else {
            cancelAlarm(context, REQUEST_QURAN_1, QuranNotificationReceiver::class.java)
        }
        if (prefs.quranEnabled && prefs.quranSecondEnabled) {
            scheduleAlarm(context, prefs.quranHour2, prefs.quranMinute2, REQUEST_QURAN_2, QuranNotificationReceiver::class.java)
        } else {
            cancelAlarm(context, REQUEST_QURAN_2, QuranNotificationReceiver::class.java)
        }
        if (prefs.dhikrEnabled) {
            scheduleAlarm(context, prefs.dhikrHour1, prefs.dhikrMinute1, REQUEST_DHIKR_1, DhikrNotificationReceiver::class.java)
        } else {
            cancelAlarm(context, REQUEST_DHIKR_1, DhikrNotificationReceiver::class.java)
        }
        if (prefs.dhikrEnabled && prefs.dhikrSecondEnabled) {
            scheduleAlarm(context, prefs.dhikrHour2, prefs.dhikrMinute2, REQUEST_DHIKR_2, DhikrNotificationReceiver::class.java)
        } else {
            cancelAlarm(context, REQUEST_DHIKR_2, DhikrNotificationReceiver::class.java)
        }
    }
}

data class NotificationPrefs(
    val quranEnabled: Boolean,
    val quranHour1: Int, val quranMinute1: Int,
    val quranSecondEnabled: Boolean,
    val quranHour2: Int, val quranMinute2: Int,
    val dhikrEnabled: Boolean,
    val dhikrHour1: Int, val dhikrMinute1: Int,
    val dhikrSecondEnabled: Boolean,
    val dhikrHour2: Int, val dhikrMinute2: Int
)
