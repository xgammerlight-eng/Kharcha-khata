package com.kharchakhata.app

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class UpiNotificationListener : NotificationListenerService() {

    companion object {
        val WATCHED_PACKAGES = setOf(
            "com.google.android.apps.nbu.paisa.user",
            "com.phonepe.app",
            "net.one97.paytm",
            "in.org.npci.upiapp",
            "com.dreamplug.androidapp"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in WATCHED_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val combined = "$title. $text".trim()
        if (combined.isBlank()) return

        val looksLikePayment = Regex("(?i)(paid|debited|sent|received|credited)").containsMatchIn(combined)
        if (!looksLikePayment) return

        val intent = Intent("com.kharchakhata.app.UPI_NOTIFICATION").apply {
            putExtra("text", combined)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}
