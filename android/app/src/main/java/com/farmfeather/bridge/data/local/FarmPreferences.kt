package com.farmfeather.bridge.data.local

import android.content.Context
import android.content.SharedPreferences

class FarmPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    var unlockedMission: Int
        get() = prefs.getInt(KEY_UNLOCKED, 1)
        set(value) = prefs.edit().putInt(KEY_UNLOCKED, value).apply()

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 0)
        set(value) = prefs.edit().putInt(KEY_COINS, value).apply()

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST, 0)
        set(value) = prefs.edit().putInt(KEY_BEST, value).apply()

    var medals: String
        get() = prefs.getString(KEY_MEDALS, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_MEDALS, value).apply()

    var missionScores: String
        get() = prefs.getString(KEY_SCORES, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_SCORES, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var cartFlashEnabled: Boolean
        get() = prefs.getBoolean(KEY_CART_FLASH, true)
        set(value) = prefs.edit().putBoolean(KEY_CART_FLASH, value).apply()

    var largeLabelsEnabled: Boolean
        get() = prefs.getBoolean(KEY_LARGE_LABELS, false)
        set(value) = prefs.edit().putBoolean(KEY_LARGE_LABELS, value).apply()

    fun writeProgress(unlocked: Int, coinTotal: Int, best: Int, medalCsv: String, scoreCsv: String): Boolean {
        return prefs.edit()
            .putInt(KEY_UNLOCKED, unlocked)
            .putInt(KEY_COINS, coinTotal)
            .putInt(KEY_BEST, best)
            .putString(KEY_MEDALS, medalCsv)
            .putString(KEY_SCORES, scoreCsv)
            .commit()
    }

    fun clearProgress(): Boolean {
        return prefs.edit()
            .remove(KEY_UNLOCKED)
            .remove(KEY_COINS)
            .remove(KEY_BEST)
            .remove(KEY_MEDALS)
            .remove(KEY_SCORES)
            .commit()
    }

    private companion object {
        const val STORE_NAME = "farm_feather_bridge"
        const val KEY_UNLOCKED = "unlocked_mission"
        const val KEY_COINS = "corn_coins"
        const val KEY_BEST = "best_score"
        const val KEY_MEDALS = "mission_medals"
        const val KEY_SCORES = "mission_scores"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_CART_FLASH = "cart_flash_enabled"
        const val KEY_LARGE_LABELS = "large_labels_enabled"
    }
}
