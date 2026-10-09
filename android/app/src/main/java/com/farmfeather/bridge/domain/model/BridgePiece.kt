package com.farmfeather.bridge.domain.model

data class BridgePiece(
    val kind: PieceKind,
    val rotation: Int
) {

    fun rotated(): BridgePiece = copy(rotation = (rotation + 90) % 360)

    fun openEdges(): Set<Int> = when (kind) {
        PieceKind.STRAIGHT -> if (rotation % 180 == 0) setOf(NORTH, SOUTH) else setOf(EAST, WEST)
        PieceKind.CORNER -> when (rotation % 360) {
            0 -> setOf(NORTH, EAST)
            90 -> setOf(EAST, SOUTH)
            180 -> setOf(SOUTH, WEST)
            else -> setOf(WEST, NORTH)
        }
    }

    companion object {
        const val NORTH = 0
        const val EAST = 1
        const val SOUTH = 2
        const val WEST = 3

        fun opposite(edge: Int): Int = (edge + 2) % 4
    }
}
