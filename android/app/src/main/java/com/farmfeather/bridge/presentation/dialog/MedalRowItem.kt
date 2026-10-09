package com.farmfeather.bridge.presentation.dialog

import com.farmfeather.bridge.domain.model.Medal

data class MedalRowItem(
    val missionId: Int,
    val missionName: String,
    val medal: Medal,
    val bestScore: Int,
    val unlocked: Boolean
)
