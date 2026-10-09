package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.Medal

class AwardMedalUseCase {

    fun medalFor(saved: Int, planksLeft: Int): Medal = when {
        saved >= GameConfig.FLOCK_SIZE && planksLeft >= 2 -> Medal.GOLD
        saved >= 4 -> Medal.SILVER
        saved >= 2 -> Medal.BRONZE
        else -> Medal.NONE
    }

    fun scoreFor(saved: Int, planksLeft: Int, secondsLeft: Int): Int =
        saved * GameConfig.SCORE_PER_BIRD +
            planksLeft * GameConfig.SCORE_PER_PLANK +
            secondsLeft * GameConfig.SCORE_PER_SECOND
}
