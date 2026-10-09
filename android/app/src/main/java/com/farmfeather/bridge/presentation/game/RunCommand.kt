package com.farmfeather.bridge.presentation.game

import com.farmfeather.bridge.domain.model.BirdPlan

data class RunCommand(
    val path: List<Int>,
    val plans: List<BirdPlan>,
    val startUptime: Long,
    val durationMs: Long
)
