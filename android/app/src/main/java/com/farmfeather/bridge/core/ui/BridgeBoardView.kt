package com.farmfeather.bridge.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.BirdPlan
import com.farmfeather.bridge.domain.model.Board
import com.farmfeather.bridge.domain.model.BridgePiece
import com.farmfeather.bridge.domain.model.CartSchedule
import com.farmfeather.bridge.domain.model.CellType
import com.farmfeather.bridge.domain.model.PieceKind
import kotlin.math.floor
import kotlin.math.min

class BridgeBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCellTap: ((Int, Int) -> Unit)? = null
    var onCellLongPress: ((Int, Int) -> Unit)? = null
    var onRunFinished: (() -> Unit)? = null

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cellRect = RectF()
    private val frameRect = RectF()
    private val spriteBounds = Rect()

    private var board: Board? = null
    private var hasRoads = false
    private var pieces: Map<Int, BridgePiece> = emptyMap()
    private var routeCells: List<Int> = emptyList()
    private var routeReady = false
    private var largeLabels = false

    private var runPath: List<Int> = emptyList()
    private var runPlans: List<BirdPlan> = emptyList()
    private var runStartUptime = 0L
    private var runDurationMs = 0L
    private var running = false
    private var runReported = false

    private val popTimes = HashMap<Int, Long>()

    private var tile = 0
    private var frameSize = 0
    private var boardWidth = 0
    private var boardHeight = 0

    private val colorGrass = ContextCompat.getColor(context, R.color.farm_grass)
    private val colorWater = ContextCompat.getColor(context, R.color.farm_water)
    private val colorWaterLight = ContextCompat.getColor(context, R.color.farm_water_light)
    private val colorRoad = ContextCompat.getColor(context, R.color.farm_road)
    private val colorRoadLine = ContextCompat.getColor(context, R.color.farm_road_line)
    private val colorRock = ContextCompat.getColor(context, R.color.farm_rock)
    private val colorWood = ContextCompat.getColor(context, R.color.farm_wood)
    private val colorSurface = ContextCompat.getColor(context, R.color.farm_surface)
    private val colorGlow = ContextCompat.getColor(context, R.color.farm_route_glow)
    private val colorPrimary = ContextCompat.getColor(context, R.color.farm_primary)
    private val colorSpeckle = ContextCompat.getColor(context, R.color.farm_success)

    private val plankStraight: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.sprite_plank_straight)
    private val plankCorner: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.sprite_plank_corner)
    private val coopSprite: Drawable? = ContextCompat.getDrawable(context, R.drawable.sprite_coop)
    private val barnSprite: Drawable? = ContextCompat.getDrawable(context, R.drawable.sprite_barn)
    private val cartSprite: Drawable? = ContextCompat.getDrawable(context, R.drawable.sprite_cart)
    private val chickSprite: Drawable? = ContextCompat.getDrawable(context, R.drawable.sprite_chick)

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                dispatchCell(event.x, event.y, longPress = false)
                return true
            }

            override fun onLongPress(event: MotionEvent) {
                dispatchCell(event.x, event.y, longPress = true)
            }
        }
    )

    init {
        frameSize = dp(BOARD_PAD_DP) + dp(BOARD_BORDER_DP)
        strokePaint.strokeWidth = dp(BOARD_BORDER_DP).toFloat()
        isClickable = true
        isFocusable = true
    }

    fun setBoard(value: Board?) {
        if (board === value) return
        board = value
        hasRoads = value?.roadRows()?.isNotEmpty() == true
        popTimes.clear()
        requestLayout()
        invalidate()
    }

    fun needsAnimation(): Boolean = running || hasRoads || popTimes.isNotEmpty()

    fun setPieces(value: Map<Int, BridgePiece>) {
        pieces = value
        invalidate()
    }

    fun setRoute(cells: List<Int>, ready: Boolean) {
        routeCells = cells
        routeReady = ready
        invalidate()
    }

    fun setLargeLabels(value: Boolean) {
        largeLabels = value
        invalidate()
    }

    fun popCell(key: Int) {
        popTimes[key] = SystemClock.uptimeMillis()
        invalidate()
    }

    fun startRun(path: List<Int>, plans: List<BirdPlan>, startUptime: Long, durationMs: Long) {
        runPath = path
        runPlans = plans
        runStartUptime = startUptime
        runDurationMs = durationMs
        running = true
        runReported = false
        invalidate()
    }

    fun stopRun() {
        running = false
        runPlans = emptyList()
        runPath = emptyList()
        invalidate()
    }

    fun onTick() {
        if (running && !runReported) {
            val elapsed = SystemClock.uptimeMillis() - runStartUptime
            if (elapsed >= runDurationMs) {
                runReported = true
                running = false
                onRunFinished?.invoke()
            }
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        val cappedWidth = if (availableWidth > 0) min(availableWidth, dp(BOARD_MAX_DP)) else dp(BOARD_MAX_DP)
        var tileSize = (cappedWidth - 2 * frameSize) / GameConfig.COLS
        if (availableHeight > 0 && MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            val byHeight = (availableHeight - 2 * frameSize) / GameConfig.ROWS
            if (byHeight > 0) tileSize = min(tileSize, byHeight)
        }
        if (tileSize < dp(MIN_TILE_DP)) tileSize = dp(MIN_TILE_DP)
        tile = tileSize
        boardWidth = tile * GameConfig.COLS + 2 * frameSize
        boardHeight = tile * GameConfig.ROWS + 2 * frameSize
        setMeasuredDimension(boardWidth, boardHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val activeBoard = board ?: return
        if (tile <= 0) return

        frameRect.set(0f, 0f, boardWidth.toFloat(), boardHeight.toFloat())
        fillPaint.color = colorSurface
        canvas.drawRoundRect(frameRect, dp(FRAME_RADIUS_DP).toFloat(), dp(FRAME_RADIUS_DP).toFloat(), fillPaint)

        val now = SystemClock.uptimeMillis()
        drawCells(canvas, activeBoard, now)
        drawRoute(canvas, activeBoard)
        drawPieces(canvas, activeBoard, now)
        drawMarkers(canvas, activeBoard)
        drawCarts(canvas, activeBoard, now)
        if (running) drawBirds(canvas, activeBoard, now)

        val inset = dp(BOARD_BORDER_DP).toFloat() / 2f
        frameRect.set(inset, inset, boardWidth - inset, boardHeight - inset)
        strokePaint.color = colorWood
        canvas.drawRoundRect(frameRect, dp(FRAME_RADIUS_DP).toFloat(), dp(FRAME_RADIUS_DP).toFloat(), strokePaint)
    }

    private fun drawCells(canvas: Canvas, activeBoard: Board, now: Long) {
        for (row in 0 until activeBoard.rows) {
            for (col in 0 until activeBoard.cols) {
                setCellRect(row, col, popScale(activeBoard.key(row, col), now))
                val type = activeBoard.typeAt(row, col)
                fillPaint.color = baseColor(type)
                val radius = dp(CELL_RADIUS_DP).toFloat()
                canvas.drawRoundRect(cellRect, radius, radius, fillPaint)
                when (type) {
                    CellType.WATER -> drawWaves(canvas)
                    CellType.ROAD -> drawRoadLine(canvas)
                    CellType.ROCK -> drawRockFace(canvas)
                    CellType.GRASS -> drawSpeckles(canvas, row, col)
                    else -> drawSpeckles(canvas, row, col)
                }
            }
        }
    }

    private fun drawRoute(canvas: Canvas, activeBoard: Board) {
        if (routeCells.size < 2) return
        glowPaint.color = colorGlow
        val radius = dp(CELL_RADIUS_DP).toFloat()
        for (key in routeCells) {
            setCellRect(activeBoard.rowOf(key), activeBoard.colOf(key), 1f)
            canvas.drawRoundRect(cellRect, radius, radius, glowPaint)
        }
        if (!routeReady) return
        dashPaint.color = colorPrimary
        dashPaint.strokeWidth = dp(ROUTE_LINE_DP).toFloat()
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(dp(6).toFloat(), dp(5).toFloat()), 0f)
        for (index in 0 until routeCells.size - 1) {
            val from = routeCells[index]
            val to = routeCells[index + 1]
            canvas.drawLine(
                centerX(activeBoard.colOf(from)),
                centerY(activeBoard.rowOf(from)),
                centerX(activeBoard.colOf(to)),
                centerY(activeBoard.rowOf(to)),
                dashPaint
            )
        }
    }

    private fun drawPieces(canvas: Canvas, activeBoard: Board, now: Long) {
        for (entry in pieces) {
            val key = entry.key
            val piece = entry.value
            val drawable = if (piece.kind == PieceKind.STRAIGHT) plankStraight else plankCorner
            drawable ?: continue
            setCellRect(activeBoard.rowOf(key), activeBoard.colOf(key), popScale(key, now))
            val inset = dp(PIECE_INSET_DP)
            spriteBounds.set(
                (cellRect.left + inset).toInt(),
                (cellRect.top + inset).toInt(),
                (cellRect.right - inset).toInt(),
                (cellRect.bottom - inset).toInt()
            )
            canvas.save()
            canvas.rotate(piece.rotation.toFloat(), cellRect.centerX(), cellRect.centerY())
            drawable.bounds = spriteBounds
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawMarkers(canvas: Canvas, activeBoard: Board) {
        val scale = if (largeLabels) MARKER_SCALE_LARGE else MARKER_SCALE
        for (cell in activeBoard.cells) {
            val drawable = when (cell.type) {
                CellType.COOP -> coopSprite
                CellType.BARN -> barnSprite
                else -> null
            } ?: continue
            setCellRect(cell.row, cell.col, 1f)
            val size = (tile * scale).toInt()
            val cx = cellRect.centerX().toInt()
            val cy = cellRect.centerY().toInt()
            spriteBounds.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
            drawable.bounds = spriteBounds
            drawable.draw(canvas)
        }
    }

    private fun drawCarts(canvas: Canvas, activeBoard: Board, now: Long) {
        val drawable = cartSprite ?: return
        val size = (tile * CART_SCALE).toInt()
        for (row in activeBoard.roadRows()) {
            val column = CartSchedule.column(now)
            val cx = (frameSize + column * tile + tile / 2f).toInt()
            val cy = centerY(row).toInt()
            spriteBounds.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
            drawable.bounds = spriteBounds
            drawable.draw(canvas)
        }
    }

    private fun drawBirds(canvas: Canvas, activeBoard: Board, now: Long) {
        val drawable = chickSprite ?: return
        if (runPath.isEmpty()) return
        val elapsed = now - runStartUptime
        val size = (tile * BIRD_SCALE).toInt()
        for (plan in runPlans) {
            val local = elapsed - plan.departMs
            if (local < 0) continue
            val rawStep = local.toFloat() / GameConfig.BIRD_STEP_MS.toFloat()
            val limit = if (plan.scatterStep >= 0) plan.scatterStep.toFloat() else runPath.lastIndex.toFloat()
            val step = if (rawStep > limit) limit else rawStep
            var alpha = 255
            var bloat = 1f
            if (plan.scatterStep >= 0 && rawStep >= limit) {
                val overshoot = local - (plan.scatterStep * GameConfig.BIRD_STEP_MS)
                val progress = overshoot.toFloat() / GameConfig.BIRD_SCATTER_MS.toFloat()
                if (progress >= 1f) continue
                alpha = ((1f - progress) * 255f).toInt()
                bloat = 1f + progress * SCATTER_BLOAT
            }
            val index = floor(step).toInt().coerceIn(0, runPath.lastIndex)
            val nextIndex = min(index + 1, runPath.lastIndex)
            val fraction = step - index
            val fromKey = runPath[index]
            val toKey = runPath[nextIndex]
            val x = centerX(activeBoard.colOf(fromKey)) +
                (centerX(activeBoard.colOf(toKey)) - centerX(activeBoard.colOf(fromKey))) * fraction
            val y = centerY(activeBoard.rowOf(fromKey)) +
                (centerY(activeBoard.rowOf(toKey)) - centerY(activeBoard.rowOf(fromKey))) * fraction
            val drawSize = (size * bloat).toInt()
            spriteBounds.set(
                (x - drawSize / 2f).toInt(),
                (y - drawSize / 2f).toInt(),
                (x + drawSize / 2f).toInt(),
                (y + drawSize / 2f).toInt()
            )
            drawable.alpha = alpha
            drawable.bounds = spriteBounds
            drawable.draw(canvas)
            drawable.alpha = 255
        }
    }

    private fun drawWaves(canvas: Canvas) {
        strokePaint.color = colorWaterLight
        strokePaint.strokeWidth = dp(1).toFloat()
        val first = cellRect.top + cellRect.height() * 0.36f
        val second = cellRect.top + cellRect.height() * 0.64f
        canvas.drawLine(cellRect.left + tile * 0.18f, first, cellRect.right - tile * 0.18f, first, strokePaint)
        canvas.drawLine(cellRect.left + tile * 0.28f, second, cellRect.right - tile * 0.28f, second, strokePaint)
        strokePaint.strokeWidth = dp(BOARD_BORDER_DP).toFloat()
    }

    private fun drawRoadLine(canvas: Canvas) {
        dashPaint.color = colorRoadLine
        dashPaint.strokeWidth = dp(2).toFloat()
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(dp(5).toFloat(), dp(4).toFloat()), 0f)
        val y = cellRect.centerY()
        canvas.drawLine(cellRect.left, y, cellRect.right, y, dashPaint)
    }

    private fun drawRockFace(canvas: Canvas) {
        fillPaint.color = colorWood
        canvas.drawCircle(cellRect.centerX(), cellRect.centerY(), tile * 0.16f, fillPaint)
    }

    private fun drawSpeckles(canvas: Canvas, row: Int, col: Int) {
        fillPaint.color = colorSpeckle
        fillPaint.alpha = 56
        val offsetX = if ((row + col) % 2 == 0) 0.32f else 0.68f
        canvas.drawCircle(
            cellRect.left + tile * offsetX,
            cellRect.top + tile * 0.3f,
            tile * 0.045f,
            fillPaint
        )
        canvas.drawCircle(
            cellRect.left + tile * (1f - offsetX),
            cellRect.top + tile * 0.68f,
            tile * 0.04f,
            fillPaint
        )
        fillPaint.alpha = 255
    }

    private fun baseColor(type: CellType): Int = when (type) {
        CellType.WATER -> colorWater
        CellType.ROAD -> colorRoad
        CellType.ROCK -> colorRock
        else -> colorGrass
    }

    private fun popScale(key: Int, now: Long): Float {
        val started = popTimes[key] ?: return 1f
        val elapsed = now - started
        if (elapsed > POP_MS) {
            popTimes.remove(key)
            return 1f
        }
        val half = POP_MS / 2f
        val progress = if (elapsed < half) elapsed / half else (POP_MS - elapsed) / half
        return 1f + POP_AMOUNT * progress
    }

    private fun setCellRect(row: Int, col: Int, scale: Float) {
        val left = frameSize + col * tile + CELL_GAP
        val top = frameSize + row * tile + CELL_GAP
        val right = frameSize + (col + 1) * tile - CELL_GAP
        val bottom = frameSize + (row + 1) * tile - CELL_GAP
        cellRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        if (scale != 1f) {
            val growX = cellRect.width() * (scale - 1f) / 2f
            val growY = cellRect.height() * (scale - 1f) / 2f
            cellRect.set(
                cellRect.left - growX,
                cellRect.top - growY,
                cellRect.right + growX,
                cellRect.bottom + growY
            )
        }
    }

    private fun centerX(col: Int): Float = frameSize + col * tile + tile / 2f

    private fun centerY(row: Int): Float = frameSize + row * tile + tile / 2f

    private fun dispatchCell(x: Float, y: Float, longPress: Boolean) {
        val activeBoard = board ?: return
        if (tile <= 0) return
        val col = ((x - frameSize) / tile).toInt()
        val row = ((y - frameSize) / tile).toInt()
        if (!activeBoard.contains(row, col)) return
        if (longPress) onCellLongPress?.invoke(row, col) else onCellTap?.invoke(row, col)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val BOARD_MAX_DP = 340
        const val BOARD_PAD_DP = 8
        const val BOARD_BORDER_DP = 2
        const val MIN_TILE_DP = 26
        const val FRAME_RADIUS_DP = 14
        const val CELL_RADIUS_DP = 7
        const val PIECE_INSET_DP = 3
        const val ROUTE_LINE_DP = 3
        const val CELL_GAP = 2
        const val POP_MS = 180L
        const val POP_AMOUNT = 0.08f
        const val MARKER_SCALE = 0.78f
        const val MARKER_SCALE_LARGE = 0.92f
        const val CART_SCALE = 0.92f
        const val BIRD_SCALE = 0.62f
        const val SCATTER_BLOAT = 0.4f
    }
}
