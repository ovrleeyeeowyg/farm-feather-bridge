package com.farmfeather.bridge.domain.model

data class BirdPlan(
    val index: Int,
    val departMs: Long,
    val scatterStep: Int
) {
    val reachesBarn: Boolean
        get() = scatterStep < 0
}
