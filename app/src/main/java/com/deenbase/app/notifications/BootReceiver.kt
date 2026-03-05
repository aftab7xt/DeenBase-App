package com.deenbase.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            val sm = SettingsManager(context)
            val prefs = NotificationPrefs(
                quranEnabled = sm.quranNotifEnabled.first(),
                quranHour1 = sm.quranNotifHour1.first(),
                quranMinute1 = sm.quranNotifMinute1.first(),
                quranSecondEnabled = sm.quranNotifSecondEnabled.first(),
                quranHour2 = sm.quranNotifHour2.first(),
                quranMinute2 = sm.quranNotifMinute2.first(),
                dhikrEnabled = sm.dhikrNotifEnabled.first(),
                dhikrHour1 = sm.dhikrNotifHour1.first(),
                dhikrMinute1 = sm.dhikrNotifMinute1.first(),
                dhikrSecondEnabled = sm.dhikrNotifSecondEnabled.first(),
                dhikrHour2 = sm.dhikrNotifHour2.first(),
                dhikrMinute2 = sm.dhikrNotifMinute2.first()
            )
            NotificationHelper.rescheduleAll(context, prefs)
            NotificationHelper.scheduleAdhkarAlarms(context)
        }
    }
}
