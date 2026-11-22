package com.coinfarmer

import android.content.Context
import org.json.JSONObject
import java.util.Calendar

/** 词库 + 反风控参数（对应 adcleaner 的 recipes + AGENTS.md 规则）
 *  数据源 assets/rules.json：不重新打包也能改词库（改完杀进程重开服务） */
object Rules {

    data class RuleSet(
        val popup: List<String>, val closeWords: List<String>, val danger: List<String>,
        val earn: List<String>, val signin: List<String>, val chest: List<String>,
        val dwellMs: LongRange, val swipeMs: LongRange,
        val nightStart: Int, val nightEnd: Int
    )

    lateinit var current: RuleSet
        private set

    fun load(ctx: Context) {
        val json = JSONObject(ctx.assets.open("rules.json").bufferedReader().readText())
        fun arr(key: String) = json.getJSONArray(key).let { a ->
            (0 until a.length()).map { a.getString(it) } }
        current = RuleSet(
            popup     = arr("popup"),
            closeWords= arr("close_words"),
            danger    = arr("danger"),
            earn      = arr("earn"),
            signin    = arr("signin"),
            chest     = arr("chest"),
            dwellMs   = json.getLong("dwell_min_ms")..json.getLong("dwell_max_ms"),
            swipeMs   = json.getLong("swipe_min_ms")..json.getLong("swipe_max_ms"),
            nightStart= json.getInt("night_start"),
            nightEnd  = json.getInt("night_end")
        )
    }

    fun nightOff(): Boolean {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return h in current.nightStart until current.nightEnd
    }
}
