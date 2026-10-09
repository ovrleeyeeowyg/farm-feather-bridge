package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.BirdPlan
import com.farmfeather.bridge.domain.model.Board
import com.farmfeather.bridge.domain.model.CartSchedule
import com.farmfeather.bridge.domain.model.CellType
import com.farmfeather.bridge.domain.model.FlockRunResult
import com.farmfeather.bridge.domain.model.PathResult

class SimulateFlockRunUseCase {

    operator fun invoke(board: Board, path: PathResult, startClockMs: Long): FlockRunResult {
        val steps = path.cells
        val plans = ArrayList<BirdPlan>(GameConfig.FLOCK_SIZE)
        for (index in 0 until GameConfig.FLOCK_SIZE) {
            val departMs = index * GameConfig.BIRD_SPAWN_GAP_MS
            plans.add(BirdPlan(index, departMs, scatterStep(board, path, startClockMs, departMs)))
        }
        val walkMs = if (steps.isEmpty()) 0L else (steps.size - 1) * GameConfig.BIRD_STEP_MS
        val lastDeparture = (GameConfig.FLOCK_SIZE - 1) * GameConfig.BIRD_SPAWN_GAP_MS
        val duration = lastDeparture + walkMs + GameConfig.BIRD_SCATTER_MS + TAIL_MS
        return FlockRunResult(path, plans, duration)
    }

    private fun scatterStep(
        board: Board,
        path: PathResult,
        startClockMs: Long,
        departMs: Long
    ): Int {
        val steps = path.cells
        if (steps.isEmpty()) return 0
        for (step in steps.indices) {
            val key = steps[step]
            if (board.typeAtKey(key) != CellType.ROAD) continue
            val arrival = startClockMs + departMs + step * GameConfig.BIRD_STEP_MS
            if (CartSchedule.isBlocking(arrival, board.colOf(key))) return step
        }
        return if (path.complete) -1 else steps.lastIndex
    }

    private companion object {
        const val TAIL_MS = 500L
    }
}
