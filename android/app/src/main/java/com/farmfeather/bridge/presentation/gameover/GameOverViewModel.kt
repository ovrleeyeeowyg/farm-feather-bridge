package com.farmfeather.bridge.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.domain.usecase.LoadMissionUseCase
import com.farmfeather.bridge.domain.usecase.SaveProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameOverViewModel(
    private val saveProgressUseCase: SaveProgressUseCase,
    private val loadMissionUseCase: LoadMissionUseCase
) : ViewModel() {

    private val state = MutableStateFlow(GameOverUiState())
    val uiState: StateFlow<GameOverUiState> = state.asStateFlow()

    private var loaded = false

    fun load(initial: GameOverUiState) {
        if (loaded) return
        loaded = true
        state.value = initial
    }

    fun retrySave() {
        val current = state.value
        if (!current.saveFailed) return
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                saveProgressUseCase(current.missionId, current.medal, current.score)
            }
            state.value = state.value.copy(saveFailed = !saved)
        }
    }

    fun nextMissionId(): Int = loadMissionUseCase.next(state.value.missionId)

    fun medalOf(name: String): Medal = Medal.fromName(name)
}
