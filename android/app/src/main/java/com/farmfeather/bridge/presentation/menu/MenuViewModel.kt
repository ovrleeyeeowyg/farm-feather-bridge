package com.farmfeather.bridge.presentation.menu

import androidx.lifecycle.ViewModel
import com.farmfeather.bridge.domain.model.Mission
import com.farmfeather.bridge.domain.model.PlayerProgress
import com.farmfeather.bridge.domain.repository.MissionRepository
import com.farmfeather.bridge.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val missionRepository: MissionRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val state = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = state.asStateFlow()

    fun refresh() {
        val progress = progressRepository.load()
        val missions = missionRepository.all()
        val selected = state.value.selectedMission.coerceIn(1, progress.unlockedMission)
        state.value = buildState(progress, missions, selected)
    }

    fun selectMission(missionId: Int) {
        val current = state.value
        if (missionId > current.unlockedMission) return
        state.value = current.copy(selectedMission = missionId)
    }

    fun progressSnapshot(): PlayerProgress = progressRepository.load()

    fun missionName(missionId: Int): String = missionRepository.byId(missionId).name

    fun resetProgress(): Boolean {
        val cleared = progressRepository.reset()
        refresh()
        return cleared
    }

    private fun buildState(
        progress: PlayerProgress,
        missions: List<Mission>,
        selected: Int
    ): MenuUiState = MenuUiState(
        missions = missions,
        unlockedMission = progress.unlockedMission,
        selectedMission = selected,
        medalCount = progress.medalCount,
        totalMissions = missions.size,
        coins = progress.coins,
        bestMedal = progress.bestMedal,
        isFresh = progress.isFresh
    )
}
