package com.farmfeather.bridge.presentation.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.domain.model.BridgePiece
import com.farmfeather.bridge.domain.model.CellType
import com.farmfeather.bridge.domain.model.FlockRunResult
import com.farmfeather.bridge.domain.model.MissionOutcome
import com.farmfeather.bridge.domain.model.PieceKind
import com.farmfeather.bridge.domain.usecase.AwardMedalUseCase
import com.farmfeather.bridge.domain.usecase.BuildBoardUseCase
import com.farmfeather.bridge.domain.usecase.LoadMissionUseCase
import com.farmfeather.bridge.domain.usecase.SaveProgressUseCase
import com.farmfeather.bridge.domain.usecase.SimulateFlockRunUseCase
import com.farmfeather.bridge.domain.usecase.ValidatePathUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val loadMissionUseCase: LoadMissionUseCase,
    private val buildBoardUseCase: BuildBoardUseCase,
    private val validatePathUseCase: ValidatePathUseCase,
    private val simulateFlockRunUseCase: SimulateFlockRunUseCase,
    private val awardMedalUseCase: AwardMedalUseCase,
    private val saveProgressUseCase: SaveProgressUseCase
) : ViewModel() {

    private val state = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = state.asStateFlow()

    private val runCommandState = MutableStateFlow<RunCommand?>(null)
    val runCommand: StateFlow<RunCommand?> = runCommandState.asStateFlow()

    private val popState = MutableStateFlow(-1)
    val popCell: StateFlow<Int> = popState.asStateFlow()

    private val outcomeState = MutableStateFlow<MissionOutcome?>(null)
    val outcome: StateFlow<MissionOutcome?> = outcomeState.asStateFlow()

    private var mountUptime = 0L
    private var lastInputUptime = 0L
    private var started = false
    private var timerJob: Job? = null
    private var runResult: FlockRunResult? = null

    fun start(missionId: Int) {
        if (started) return
        started = true
        val mission = loadMissionUseCase(missionId)
        val board = buildBoardUseCase(mission)
        mountUptime = SystemClock.uptimeMillis()
        lastInputUptime = mountUptime
        state.value = GameUiState(
            missionId = mission.id,
            missionName = mission.name,
            phase = GamePhase.IDLE,
            board = board,
            pieces = emptyMap(),
            routeCells = emptyList(),
            routeReady = false,
            straightLeft = mission.straightPlanks,
            cornerLeft = mission.cornerPlanks,
            selected = if (mission.straightPlanks > 0) PieceKind.STRAIGHT else PieceKind.CORNER,
            remainingMs = GameConfig.MISSION_TIME_MS,
            flockSaved = GameConfig.FLOCK_SIZE
        )
        startTimer()
    }

    fun resumeOrRestart() {
        if (state.value.phase != GamePhase.RESULT) return
        val missionId = state.value.missionId
        started = false
        runResult = null
        outcomeState.value = null
        runCommandState.value = null
        popState.value = -1
        start(missionId)
    }

    fun selectPiece(kind: PieceKind) {
        val current = state.value
        if (!current.inputEnabled) return
        lastInputUptime = SystemClock.uptimeMillis()
        state.value = current.copy(selected = kind)
    }

    fun onCellTap(row: Int, col: Int) {
        val current = state.value
        val board = current.board ?: return
        if (!current.inputEnabled) return
        if (board.typeAt(row, col) != CellType.WATER) return
        lastInputUptime = SystemClock.uptimeMillis()
        val key = board.key(row, col)
        val existing = current.pieces[key]
        val pieces = HashMap(current.pieces)
        var straight = current.straightLeft
        var corner = current.cornerLeft
        if (existing != null) {
            pieces[key] = existing.rotated()
        } else {
            val kind = current.selected
            if (kind == PieceKind.STRAIGHT && straight <= 0) return
            if (kind == PieceKind.CORNER && corner <= 0) return
            pieces[key] = BridgePiece(kind, 0)
            if (kind == PieceKind.STRAIGHT) straight-- else corner--
        }
        popState.value = key
        applyBoardChange(pieces, straight, corner)
    }

    fun onCellLongPress(row: Int, col: Int) {
        val current = state.value
        val board = current.board ?: return
        if (!current.inputEnabled) return
        val key = board.key(row, col)
        val existing = current.pieces[key] ?: return
        lastInputUptime = SystemClock.uptimeMillis()
        val pieces = HashMap(current.pieces)
        pieces.remove(key)
        val straight = current.straightLeft + if (existing.kind == PieceKind.STRAIGHT) 1 else 0
        val corner = current.cornerLeft + if (existing.kind == PieceKind.CORNER) 1 else 0
        popState.value = key
        applyBoardChange(pieces, straight, corner)
    }

    fun onPopConsumed() {
        popState.value = -1
    }

    fun sendFlock() {
        val current = state.value
        if (!current.inputEnabled) return
        lastInputUptime = SystemClock.uptimeMillis()
        beginRun()
    }

    fun onRunFinished() {
        if (state.value.phase != GamePhase.RUNNING) return
        finishRound()
    }

    fun onOutcomeConsumed() {
        outcomeState.value = null
    }

    fun onRunConsumed() {
        runCommandState.value = null
    }

    private fun applyBoardChange(
        pieces: Map<Int, BridgePiece>,
        straightLeft: Int,
        cornerLeft: Int
    ) {
        val current = state.value
        val board = current.board ?: return
        val path = validatePathUseCase(board, pieces)
        val phase = if (path.complete) GamePhase.READY else GamePhase.BUILDING
        state.value = current.copy(
            pieces = pieces,
            straightLeft = straightLeft,
            cornerLeft = cornerLeft,
            routeCells = path.cells,
            routeReady = path.complete,
            phase = phase
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(GameConfig.TIMER_TICK_MS)
                val current = state.value
                if (current.phase == GamePhase.RESULT) break
                val now = SystemClock.uptimeMillis()
                val elapsed = now - mountUptime
                val remaining = GameConfig.MISSION_TIME_MS - elapsed
                if (current.phase != GamePhase.RUNNING) {
                    state.value = state.value.copy(
                        remainingMs = if (remaining > 0) remaining else 0L
                    )
                    if (shouldAutoRun(now)) {
                        beginRun()
                    }
                }
            }
        }
    }

    private fun shouldAutoRun(now: Long): Boolean {
        val floorMoment = mountUptime + GameConfig.IDLE_FLOOR_MS
        val idleMoment = lastInputUptime + GameConfig.IDLE_AUTO_RUN_MS
        val autoMoment = if (floorMoment > idleMoment) floorMoment else idleMoment
        val hardCap = mountUptime + GameConfig.MISSION_TIME_MS
        val trigger = if (autoMoment < hardCap) autoMoment else hardCap
        return now >= trigger
    }

    private fun beginRun() {
        val current = state.value
        val board = current.board ?: return
        if (current.phase == GamePhase.RUNNING || current.phase == GamePhase.RESULT) return
        val path = validatePathUseCase(board, current.pieces)
        val startUptime = SystemClock.uptimeMillis() + RUN_LEAD_MS
        val result = simulateFlockRunUseCase(board, path, startUptime)
        runResult = result
        state.value = current.copy(
            phase = GamePhase.RUNNING,
            routeCells = path.cells,
            routeReady = path.complete
        )
        runCommandState.value = RunCommand(
            path = path.cells,
            plans = result.plans,
            startUptime = startUptime,
            durationMs = result.durationMs
        )
        scheduleRunBackstop(result.durationMs)
    }

    private fun scheduleRunBackstop(durationMs: Long) {
        viewModelScope.launch {
            delay(durationMs + RUN_BACKSTOP_MS)
            if (state.value.phase == GamePhase.RUNNING) {
                finishRound()
            }
        }
    }

    private fun finishRound() {
        val current = state.value
        val result = runResult
        val saved = result?.saved ?: 0
        val secondsLeft = (current.remainingMs / 1000L).toInt().coerceAtLeast(0)
        val planksLeft = current.planksLeft
        val medal = awardMedalUseCase.medalFor(saved, planksLeft)
        val score = awardMedalUseCase.scoreFor(saved, planksLeft, secondsLeft)
        state.value = current.copy(phase = GamePhase.RESULT, flockSaved = saved)
        timerJob?.cancel()
        viewModelScope.launch {
            val unlocksNext = withContext(Dispatchers.IO) {
                saveProgressUseCase.unlocksNext(current.missionId, medal)
            }
            val saveOk = withContext(Dispatchers.IO) {
                saveProgressUseCase(current.missionId, medal, score)
            }
            delay(GameConfig.RESULT_DELAY_MS)
            outcomeState.value = MissionOutcome(
                missionId = current.missionId,
                missionName = current.missionName,
                medal = medal,
                score = score,
                coins = medal.coins,
                saved = saved,
                flockSize = GameConfig.FLOCK_SIZE,
                planksLeft = planksLeft,
                secondsLeft = secondsLeft,
                unlockedNext = unlocksNext,
                saveFailed = !saveOk
            )
        }
    }

    fun nextMissionId(): Int = loadMissionUseCase.next(state.value.missionId)

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val RUN_LEAD_MS = 120L
        const val RUN_BACKSTOP_MS = 900L
    }
}
