package com.farmfeather.bridge.domain.model

enum class Medal(val rank: Int, val coins: Int) {
    NONE(0, 0),
    BRONZE(1, 20),
    SILVER(2, 40),
    GOLD(3, 60);

    companion object {
        fun fromName(value: String): Medal = values().firstOrNull { it.name == value } ?: NONE
    }
}
