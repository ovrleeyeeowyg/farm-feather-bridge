package com.farmfeather.bridge.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.farmfeather.bridge.core.di.ServiceLocator
import com.farmfeather.bridge.presentation.game.GameViewModel
import com.farmfeather.bridge.presentation.gameover.GameOverViewModel
import com.farmfeather.bridge.presentation.menu.MenuViewModel
import com.farmfeather.bridge.presentation.splash.SplashViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel()
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(
                ServiceLocator.missionRepository,
                ServiceLocator.progressRepository
            )
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(
                ServiceLocator.loadMissionUseCase,
                ServiceLocator.buildBoardUseCase,
                ServiceLocator.validatePathUseCase,
                ServiceLocator.simulateFlockRunUseCase,
                ServiceLocator.awardMedalUseCase,
                ServiceLocator.saveProgressUseCase
            )
            modelClass.isAssignableFrom(GameOverViewModel::class.java) -> GameOverViewModel(
                ServiceLocator.saveProgressUseCase,
                ServiceLocator.loadMissionUseCase
            )
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
        return modelClass.cast(created)
    }
}
