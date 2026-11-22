package com.coinfarmer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Notifier {
    private const val CH = "farmer"
    fun status(ctx: Context, msg: String) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CH, "farmer", NotificationManager.IMPORTANCE_LOW))
        nm.notify(1, NotificationCompat.Builder(ctx, CH)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("coin-farmer").setContentText(msg).build())
    }
}

object EarnLog {
    fun append(ctx: Context, hits: List<String>) {
        File(ctx.filesDir, "earn.log").appendText(
            "${SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA).format(Date())}\t${hits.joinToString("|")}\n")
    }
}
