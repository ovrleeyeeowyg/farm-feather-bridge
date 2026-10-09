package com.farmfeather.bridge.presentation.gameover

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.Medal

data class GameOverUiState(
    val missionId: Int = 1,
    val missionName: String = "",
    val medal: Medal = Medal.NONE,
    val score: Int = 0,
    val coins: Int = 0,
    val saved: Int = 0,
    val flockSize: Int = GameConfig.FLOCK_SIZE,
    val planksLeft: Int = 0,
    val secondsLeft: Int = 0,
    val unlockedNext: Boolean = false,
    val saveFailed: Boolean = false
) {
    val showPlanks: Boolean
        get() = planksLeft > 0

    val showCoins: Boolean
        get() = coins > 0
}
