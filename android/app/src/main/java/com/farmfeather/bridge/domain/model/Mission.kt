package com.farmfeather.bridge.domain.model

data class Mission(
    val id: Int,
    val name: String,
    val hint: String,
    val grid: List<String>,
    val straightPlanks: Int,
    val cornerPlanks: Int
) {
    val totalPlanks: Int
        get() = straightPlanks + cornerPlanks
}
