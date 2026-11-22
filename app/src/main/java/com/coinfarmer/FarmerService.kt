package com.coinfarmer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Random

class FarmerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val rnd = Random()
    private var running = false
    private var mode = Mode.FEED
    private val rules get() = Rules.current

    enum class Mode { FEED, SIGNIN }

    override fun onServiceConnected() {
        Rules.load(this)
        running = true
        SignInPlanner.scheduleDaily(this)
        postLoop(0)
    }

    override fun onDestroy() { running = false; handler.removeCallbacksAndMessages(null) }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            running = false
            notifyStatus("已停止。重开无障碍服务可继续。")
            return true
        }
        return super.onKeyEvent(event)
    }

    private fun postLoop(delayMs: Long) = handler.postDelayed({ tick() }, delayMs)

    private fun tick() {
        if (!running) return
        if (Rules.nightOff()) { postLoop(30 * 60_000); return }

        val root = rootInActiveWindow
        if (root == null) { postLoop(5_000); return }

        val texts = dumpTexts(root)
        val joined = texts.joinToString(" ")

        when {
            rules.danger.any { it in joined } -> {
                notifyStatus("出现支付类界面，已暂停")
                postLoop(60_000); return
            }
            mode == Mode.SIGNIN && handleSignIn(root, texts) -> {}
            rules.popup.any { it in joined } -> dismissPopup(root, texts)
            else -> { logEarn(texts); swipeUp() }
        }

        var dwell = rules.dwellMs.random(rnd)
        if (rnd.nextDouble() < 0.10) dwell *= 2
        postLoop(dwell)
    }

    private fun swipeUp() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels * (0.5 + (rnd.nextDouble() - 0.5) * 0.10)
        val y1 = dm.heightPixels * (0.72 + rnd.nextDouble() * 0.14)
        val y2 = dm.heightPixels * (0.14 + rnd.nextDouble() * 0.12)
        if (rnd.nextDouble() < 0.06) {
            val mid = (y1 + y2) / 2
            gesture(x, y1, x, mid, rules.swipeMs.random(rnd))
            handler.postDelayed({ gesture(x, mid, x, y2, rules.swipeMs.random(rnd)) },
                (1500 + rnd.nextInt(2500)).toLong())
        } else {
            gesture(x, y1, x, y2, rules.swipeMs.random(rnd))
        }
    }

    private fun gesture(x1: Float, y1: Float, x2: Float, y2: Float, ms: Long) {
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val stroke = GestureDescription.StrokeDescription(path, 0, ms)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun clickText(root: AccessibilityNodeInfo, vararg words: String): Boolean {
        for (w in words) {
            val nodes = root.findAccessibilityNodeInfosByText(w) ?: continue
            for (n in nodes) { if (clickNode(n)) return true }
        }
        return false
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = node
        for (depth in 0..3) {
            if (n == null) return false
            if (n.isClickable && n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            n = n.parent
        }
        val r = android.graphics.Rect(); node.getBoundsInScreen(r)
        gesture(r.centerX().toFloat(), r.centerY().toFloat(),
                r.centerX().toFloat(), r.centerY().toFloat(), 80)
        return true
    }

    private fun dismissPopup(root: AccessibilityNodeInfo, texts: List<String>) {
        if (clickText(root, *rules.closeWords.toTypedArray())) return
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun handleSignIn(root: AccessibilityNodeInfo, texts: List<String>): Boolean {
        val joined = texts.joinToString(" ")
        return when {
            rules.signin.any { it in joined } -> clickText(root, *rules.signin.toTypedArray())
            rules.chest.any  { it in joined } -> clickText(root, *rules.chest.toTypedArray())
            rules.popup.any  { it in joined } -> { dismissPopup(root, texts); true }
            else -> false
        }
    }

    private fun logEarn(texts: List<String>) {
        val hits = texts.filter { t -> rules.earn.any { it in t } }
        if (hits.isNotEmpty()) EarnLog.append(this, hits.take(5))
    }

    private fun dumpTexts(root: AccessibilityNodeInfo, limit: Int = 40): List<String> {
        val out = ArrayList<String>()
        fun walk(n: AccessibilityNodeInfo?, depth: Int) {
            if (n == null || out.size >= limit || depth > 12) return
            n.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
            for (i in 0 until n.childCount) walk(n.getChild(i), depth + 1)
        }
        walk(root, 0)
        return out
    }

    private fun notifyStatus(msg: String) = Notifier.status(this, msg)
}
