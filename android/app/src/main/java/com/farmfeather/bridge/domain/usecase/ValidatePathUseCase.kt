package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.domain.model.Board
import com.farmfeather.bridge.domain.model.BridgePiece
import com.farmfeather.bridge.domain.model.CellType
import com.farmfeather.bridge.domain.model.PathResult

class ValidatePathUseCase {

    operator fun invoke(board: Board, pieces: Map<Int, BridgePiece>): PathResult {
        val start = board.firstOf(CellType.COOP)
        val goal = board.firstOf(CellType.BARN)
        if (start == goal) return PathResult.blocked()

        val parents = HashMap<Int, Int>()
        val distance = HashMap<Int, Int>()
        val queue = ArrayDeque<Int>()
        queue.add(start)
        distance[start] = 0

        var farthest = start
        var farthestDistance = 0

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDistance = distance[current] ?: 0
            if (currentDistance > farthestDistance) {
                farthestDistance = currentDistance
                farthest = current
            }
            if (current == goal) break
            val row = board.rowOf(current)
            val col = board.colOf(current)
            val edges = openEdges(board, pieces, current)
            for (edge in edges) {
                val nextRow = row + rowDelta(edge)
                val nextCol = col + colDelta(edge)
                if (!board.contains(nextRow, nextCol)) continue
                val nextKey = board.key(nextRow, nextCol)
                if (distance.containsKey(nextKey)) continue
                val back = BridgePiece.opposite(edge)
                if (!openEdges(board, pieces, nextKey).contains(back)) continue
                distance[nextKey] = currentDistance + 1
                parents[nextKey] = current
                queue.add(nextKey)
            }
        }

        return if (distance.containsKey(goal)) {
            PathResult(true, reconstruct(parents, start, goal))
        } else {
            PathResult(false, reconstruct(parents, start, farthest))
        }
    }

    private fun reconstruct(parents: Map<Int, Int>, start: Int, end: Int): List<Int> {
        val path = ArrayList<Int>()
        var cursor = end
        path.add(cursor)
        var guard = 0
        while (cursor != start && guard < MAX_STEPS) {
            cursor = parents[cursor] ?: return listOf(start)
            path.add(cursor)
            guard++
        }
        path.reverse()
        return path
    }

    private fun openEdges(board: Board, pieces: Map<Int, BridgePiece>, key: Int): Set<Int> {
        return when (board.typeAtKey(key)) {
            CellType.ROCK -> emptySet()
            CellType.WATER -> pieces[key]?.openEdges() ?: emptySet()
            else -> ALL_EDGES
        }
    }

    private fun rowDelta(edge: Int): Int = when (edge) {
        BridgePiece.NORTH -> -1
        BridgePiece.SOUTH -> 1
        else -> 0
    }

    private fun colDelta(edge: Int): Int = when (edge) {
        BridgePiece.EAST -> 1
        BridgePiece.WEST -> -1
        else -> 0
    }

    private companion object {
        const val MAX_STEPS = 256
        val ALL_EDGES = setOf(
            BridgePiece.NORTH,
            BridgePiece.EAST,
            BridgePiece.SOUTH,
            BridgePiece.WEST
        )
    }
}
