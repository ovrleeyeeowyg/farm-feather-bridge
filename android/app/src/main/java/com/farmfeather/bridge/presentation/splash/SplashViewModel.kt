package com.farmfeather.bridge.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmfeather.bridge.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val navigateState = MutableStateFlow(false)
    val navigate: StateFlow<Boolean> = navigateState.asStateFlow()

    private val stageState = MutableStateFlow(0)
    val stage: StateFlow<Int> = stageState.asStateFlow()

    private val progressState = MutableStateFlow(0)
    val progress: StateFlow<Int> = progressState.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            runCountdown()
            navigateState.value = true
        }
    }

    fun onNavigated() {
        navigateState.value = false
    }

    private suspend fun runCountdown() {
        val stageDuration = GameConfig.SPLASH_STAGE_MS
        val stageCount = (GameConfig.LOADER_DURATION_MS / stageDuration).toInt()
        val sliceCount = (stageDuration / PROGRESS_SLICE_MS).toInt()
        var elapsed = 0L
        for (stageIndex in 0 until stageCount) {
            stageState.value = stageIndex
            for (slice in 0 until sliceCount) {
                delay(PROGRESS_SLICE_MS)
                elapsed += PROGRESS_SLICE_MS
                progressState.value = percentOf(elapsed)
            }
        }
        val remainder = GameConfig.LOADER_DURATION_MS - elapsed
        if (remainder > 0) {
            delay(remainder)
            elapsed += remainder
            progressState.value = percentOf(elapsed)
        }
    }

    private fun percentOf(elapsed: Long): Int {
        val total = GameConfig.LOADER_DURATION_MS
        if (total <= 0L) return 100
        val value = (elapsed * 100L / total).toInt()
        return if (value > 100) 100 else value
    }

    private companion object {
        const val PROGRESS_SLICE_MS = 100L
    }
}
