package com.coinfarmer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

object SignInPlanner {
    private val SLOTS = listOf(8, 12, 20)

    fun scheduleDaily(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        SLOTS.forEach { h ->
            val pi = PendingIntent.getBroadcast(
                ctx, h, Intent(ctx, SignInReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }
}

class SignInReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val launch = ctx.packageManager.getLaunchIntentForPackage("com.kuaishou.nebula")
        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(launch)
        ctx.getSharedPreferences("farmer", Context.MODE_PRIVATE)
            .edit().putBoolean("pending_signin", true).apply()
        Notifier.status(ctx, "进入签到模式")
    }
}
