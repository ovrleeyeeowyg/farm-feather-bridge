package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.Board
import com.farmfeather.bridge.domain.model.BoardCell
import com.farmfeather.bridge.domain.model.CellType
import com.farmfeather.bridge.domain.model.Mission

class BuildBoardUseCase {

    operator fun invoke(mission: Mission): Board {
        val rows = GameConfig.ROWS
        val cols = GameConfig.COLS
        val cells = ArrayList<BoardCell>(rows * cols)
        for (row in 0 until rows) {
            val line = mission.grid.getOrNull(row).orEmpty()
            for (col in 0 until cols) {
                val symbol = line.getOrNull(col) ?: '.'
                cells.add(BoardCell(row, col, CellType.fromSymbol(symbol)))
            }
        }
        return Board(rows, cols, cells)
    }
}
