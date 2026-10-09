package com.farmfeather.bridge.domain.model

enum class CellType {
    GRASS,
    WATER,
    ROAD,
    ROCK,
    COOP,
    BARN;

    val isWalkable: Boolean
        get() = this != ROCK

    val needsPlank: Boolean
        get() = this == WATER

    companion object {
        fun fromSymbol(symbol: Char): CellType = when (symbol) {
            '~' -> WATER
            '=' -> ROAD
            '#' -> ROCK
            'C' -> COOP
            'B' -> BARN
            else -> GRASS
        }
    }
}
