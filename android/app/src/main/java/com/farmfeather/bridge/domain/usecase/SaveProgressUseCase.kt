package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.domain.model.PlayerProgress
import com.farmfeather.bridge.domain.repository.ProgressRepository
import kotlin.math.max

class SaveProgressUseCase(private val progressRepository: ProgressRepository) {

    operator fun invoke(missionId: Int, medal: Medal, score: Int): Boolean {
        val current = progressRepository.load()
        val previousMedal = current.medals[missionId] ?: Medal.NONE
        val bestMedal = if (medal.rank > previousMedal.rank) medal else previousMedal
        val previousScore = current.missionScores[missionId] ?: 0
        val medals = HashMap(current.medals)
        medals[missionId] = bestMedal
        val scores = HashMap(current.missionScores)
        scores[missionId] = max(previousScore, score)
        val unlocked = if (medal != Medal.NONE && missionId >= current.unlockedMission) {
            minOf(missionId + 1, GameConfig.MISSION_COUNT)
        } else {
            current.unlockedMission
        }
        val updated = PlayerProgress(
            unlockedMission = unlocked,
            coins = current.coins + medal.coins,
            bestScore = max(current.bestScore, score),
            medals = medals,
            missionScores = scores
        )
        return progressRepository.save(updated)
    }

    fun unlocksNext(missionId: Int, medal: Medal): Boolean {
        val current = progressRepository.load()
        return medal != Medal.NONE &&
            missionId >= current.unlockedMission &&
            missionId < GameConfig.MISSION_COUNT
    }
}
