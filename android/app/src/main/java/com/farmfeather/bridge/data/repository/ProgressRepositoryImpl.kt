package com.farmfeather.bridge.data.repository

import com.farmfeather.bridge.data.local.FarmPreferences
import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.domain.model.PlayerProgress
import com.farmfeather.bridge.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val preferences: FarmPreferences) : ProgressRepository {

    override fun load(): PlayerProgress = PlayerProgress(
        unlockedMission = preferences.unlockedMission,
        coins = preferences.coins,
        bestScore = preferences.bestScore,
        medals = decodeMedals(preferences.medals),
        missionScores = decodeScores(preferences.missionScores)
    )

    override fun save(progress: PlayerProgress): Boolean = preferences.writeProgress(
        unlocked = progress.unlockedMission,
        coinTotal = progress.coins,
        best = progress.bestScore,
        medalCsv = encodeMedals(progress.medals),
        scoreCsv = encodeScores(progress.missionScores)
    )

    override fun reset(): Boolean = preferences.clearProgress()

    override fun isSoundEnabled(): Boolean = preferences.soundEnabled

    override fun setSoundEnabled(enabled: Boolean) {
        preferences.soundEnabled = enabled
    }

    override fun isCartFlashEnabled(): Boolean = preferences.cartFlashEnabled

    override fun setCartFlashEnabled(enabled: Boolean) {
        preferences.cartFlashEnabled = enabled
    }

    override fun isLargeLabelsEnabled(): Boolean = preferences.largeLabelsEnabled

    override fun setLargeLabelsEnabled(enabled: Boolean) {
        preferences.largeLabelsEnabled = enabled
    }

    private fun decodeMedals(raw: String): Map<Int, Medal> {
        if (raw.isBlank()) return emptyMap()
        val result = HashMap<Int, Medal>()
        raw.split(ENTRY_SEPARATOR).forEach { entry ->
            val parts = entry.split(PAIR_SEPARATOR)
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull()
                if (id != null) result[id] = Medal.fromName(parts[1])
            }
        }
        return result
    }

    private fun encodeMedals(medals: Map<Int, Medal>): String =
        medals.entries.joinToString(ENTRY_SEPARATOR) { "${it.key}$PAIR_SEPARATOR${it.value.name}" }

    private fun decodeScores(raw: String): Map<Int, Int> {
        if (raw.isBlank()) return emptyMap()
        val result = HashMap<Int, Int>()
        raw.split(ENTRY_SEPARATOR).forEach { entry ->
            val parts = entry.split(PAIR_SEPARATOR)
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull()
                val score = parts[1].toIntOrNull()
                if (id != null && score != null) result[id] = score
            }
        }
        return result
    }

    private fun encodeScores(scores: Map<Int, Int>): String =
        scores.entries.joinToString(ENTRY_SEPARATOR) { "${it.key}$PAIR_SEPARATOR${it.value}" }

    private companion object {
        const val ENTRY_SEPARATOR = ","
        const val PAIR_SEPARATOR = ":"
    }
}
