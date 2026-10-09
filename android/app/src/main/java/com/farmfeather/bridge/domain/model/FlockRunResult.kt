package com.farmfeather.bridge.domain.model

data class FlockRunResult(
    val path: PathResult,
    val plans: List<BirdPlan>,
    val durationMs: Long
) {
    val saved: Int
        get() = plans.count { it.reachesBarn }

    val lost: Int
        get() = plans.size - saved
}
