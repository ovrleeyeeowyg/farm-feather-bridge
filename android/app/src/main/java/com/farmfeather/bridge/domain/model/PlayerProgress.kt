package com.farmfeather.bridge.domain.model

data class PlayerProgress(
    val unlockedMission: Int,
    val coins: Int,
    val bestScore: Int,
    val medals: Map<Int, Medal>,
    val missionScores: Map<Int, Int>
) {

    val medalCount: Int
        get() = medals.count { it.value != Medal.NONE }

    val bestMedal: Medal
        get() = medals.values.maxByOrNull { it.rank } ?: Medal.NONE

    val isFresh: Boolean
        get() = medalCount == 0 && coins == 0 && bestScore == 0

    companion object {
        fun empty(): PlayerProgress = PlayerProgress(
            unlockedMission = 1,
            coins = 0,
            bestScore = 0,
            medals = emptyMap(),
            missionScores = emptyMap()
        )
    }
}
