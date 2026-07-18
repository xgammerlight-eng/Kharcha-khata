package com.kharchakhata.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

private const val WEB_APP_URL = "https://your-kharcha-khata-app-url.example.com"

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val text = intent.getStringExtra("text") ?: return
            val escaped = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")
            webView.evaluateJavascript(
                "window.handleUpiNotification && window.handleUpiNotification('$escaped');",
                null
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(WEB_APP_URL)
        setContentView(webView)

        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.kharchakhata.app.UPI_NOTIFICATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(notificationReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(notificationReceiver)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled != null && enabled.contains(packageName)
    }
}
