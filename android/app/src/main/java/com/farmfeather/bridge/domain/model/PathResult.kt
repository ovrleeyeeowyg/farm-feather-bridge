package com.farmfeather.bridge.domain.model

data class PathResult(
    val complete: Boolean,
    val cells: List<Int>
) {
    companion object {
        fun blocked(): PathResult = PathResult(false, emptyList())
    }
}
