package com.farmfeather.bridge.domain.model

import com.farmfeather.bridge.core.config.GameConfig
import kotlin.math.abs

object CartSchedule {

    private const val TRAVEL_SPAN = GameConfig.COLS + 2

    fun phase(timeMs: Long): Float {
        val period = GameConfig.CART_PERIOD_MS
        val wrapped = ((timeMs % period) + period) % period
        return wrapped.toFloat() / period.toFloat()
    }

    fun column(timeMs: Long): Float = phase(timeMs) * TRAVEL_SPAN - 1f

    fun blockingHalfWidth(): Float =
        GameConfig.CART_BLOCK_MS.toFloat() / GameConfig.CART_PERIOD_MS.toFloat() * TRAVEL_SPAN / 2f

    fun isBlocking(timeMs: Long, col: Int): Boolean =
        abs(column(timeMs) - (col + 0.5f)) < blockingHalfWidth()
}
