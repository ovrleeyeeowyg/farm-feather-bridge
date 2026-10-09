package com.farmfeather.bridge.presentation.game

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.Board
import com.farmfeather.bridge.domain.model.BridgePiece
import com.farmfeather.bridge.domain.model.PieceKind

data class GameUiState(
    val missionId: Int = 1,
    val missionName: String = "",
    val phase: GamePhase = GamePhase.IDLE,
    val board: Board? = null,
    val pieces: Map<Int, BridgePiece> = emptyMap(),
    val routeCells: List<Int> = emptyList(),
    val routeReady: Boolean = false,
    val straightLeft: Int = 0,
    val cornerLeft: Int = 0,
    val selected: PieceKind = PieceKind.STRAIGHT,
    val remainingMs: Long = GameConfig.MISSION_TIME_MS,
    val flockSaved: Int = GameConfig.FLOCK_SIZE
) {
    val planksLeft: Int
        get() = straightLeft + cornerLeft

    val inputEnabled: Boolean
        get() = phase == GamePhase.IDLE || phase == GamePhase.BUILDING || phase == GamePhase.READY

    val urgent: Boolean
        get() = remainingMs <= URGENT_MS

    private companion object {
        const val URGENT_MS = 10000L
    }
}
