package com.farmfeather.bridge.presentation.menu

import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.domain.model.Mission

data class MenuUiState(
    val missions: List<Mission> = emptyList(),
    val unlockedMission: Int = 1,
    val selectedMission: Int = 1,
    val medalCount: Int = 0,
    val totalMissions: Int = 0,
    val coins: Int = 0,
    val bestMedal: Medal = Medal.NONE,
    val isFresh: Boolean = true
) {
    val showStats: Boolean
        get() = visibleStatCount >= 2

    val visibleStatCount: Int
        get() {
            var count = 0
            if (medalCount > 0) count++
            if (coins > 0) count++
            if (bestMedal != Medal.NONE) count++
            return count
        }
}
