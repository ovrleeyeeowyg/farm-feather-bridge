package com.farmfeather.bridge.domain.model

class Board(
    val rows: Int,
    val cols: Int,
    val cells: List<BoardCell>
) {

    fun key(row: Int, col: Int): Int = row * cols + col

    fun rowOf(key: Int): Int = key / cols

    fun colOf(key: Int): Int = key % cols

    fun contains(row: Int, col: Int): Boolean = row in 0 until rows && col in 0 until cols

    fun cellAt(row: Int, col: Int): BoardCell? =
        if (contains(row, col)) cells[key(row, col)] else null

    fun typeAt(row: Int, col: Int): CellType = cellAt(row, col)?.type ?: CellType.ROCK

    fun typeAtKey(key: Int): CellType = cells[key].type

    fun firstOf(type: CellType): Int {
        val index = cells.indexOfFirst { it.type == type }
        return if (index >= 0) index else 0
    }

    fun waterKeys(): List<Int> = cells.filter { it.type == CellType.WATER }.map { key(it.row, it.col) }

    fun roadRows(): List<Int> = cells.filter { it.type == CellType.ROAD }.map { it.row }.distinct()
}
