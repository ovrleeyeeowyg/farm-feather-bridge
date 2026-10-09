package com.farmfeather.bridge.domain.model

data class MissionOutcome(
    val missionId: Int,
    val missionName: String,
    val medal: Medal,
    val score: Int,
    val coins: Int,
    val saved: Int,
    val flockSize: Int,
    val planksLeft: Int,
    val secondsLeft: Int,
    val unlockedNext: Boolean,
    val saveFailed: Boolean
)
