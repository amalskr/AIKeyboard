package com.ceylonapz.aikeyboard

import android.content.Context
import android.content.SharedPreferences

class UsageStats(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun recordGrammarCheck() = bump(KEY_GRAMMAR_CHECKS)
    fun recordFixApplied() = bump(KEY_FIXES_APPLIED)
    fun recordSmartReply() = bump(KEY_SMART_REPLIES)
    fun recordReplyUsed() = bump(KEY_REPLIES_USED)

    fun snapshot(): UsageSnapshot {
        return UsageSnapshot(
            grammarChecks = prefs.getInt(KEY_GRAMMAR_CHECKS, 0),
            fixesApplied = prefs.getInt(KEY_FIXES_APPLIED, 0),
            smartReplies = prefs.getInt(KEY_SMART_REPLIES, 0),
            repliesUsed = prefs.getInt(KEY_REPLIES_USED, 0),
            lastUsedAt = prefs.getLong(KEY_LAST_USED_AT, 0L)
        )
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun bump(key: String) {
        val current = prefs.getInt(key, 0)
        prefs.edit()
            .putInt(key, current + 1)
            .putLong(KEY_LAST_USED_AT, System.currentTimeMillis())
            .apply()
    }

    companion object {
        const val PREFS_NAME = "ai_keyboard_usage"
        private const val KEY_GRAMMAR_CHECKS = "grammar_checks"
        private const val KEY_FIXES_APPLIED = "fixes_applied"
        private const val KEY_SMART_REPLIES = "smart_replies"
        private const val KEY_REPLIES_USED = "replies_used"
        private const val KEY_LAST_USED_AT = "last_used_at"
    }
}

data class UsageSnapshot(
    val grammarChecks: Int,
    val fixesApplied: Int,
    val smartReplies: Int,
    val repliesUsed: Int,
    val lastUsedAt: Long,
) {
    val totalPrompts: Int get() = grammarChecks + smartReplies
}
