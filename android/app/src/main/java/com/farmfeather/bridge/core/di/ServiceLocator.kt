package com.farmfeather.bridge.core.di

import android.content.Context
import com.farmfeather.bridge.data.local.FarmPreferences
import com.farmfeather.bridge.data.repository.MissionRepositoryImpl
import com.farmfeather.bridge.data.repository.ProgressRepositoryImpl
import com.farmfeather.bridge.domain.repository.MissionRepository
import com.farmfeather.bridge.domain.repository.ProgressRepository
import com.farmfeather.bridge.domain.usecase.AwardMedalUseCase
import com.farmfeather.bridge.domain.usecase.BuildBoardUseCase
import com.farmfeather.bridge.domain.usecase.LoadMissionUseCase
import com.farmfeather.bridge.domain.usecase.SaveProgressUseCase
import com.farmfeather.bridge.domain.usecase.SimulateFlockRunUseCase
import com.farmfeather.bridge.domain.usecase.ValidatePathUseCase

object ServiceLocator {

    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    val missionRepository: MissionRepository by lazy { MissionRepositoryImpl() }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(FarmPreferences(requireContext()))
    }

    val loadMissionUseCase: LoadMissionUseCase by lazy { LoadMissionUseCase(missionRepository) }

    val buildBoardUseCase: BuildBoardUseCase by lazy { BuildBoardUseCase() }

    val validatePathUseCase: ValidatePathUseCase by lazy { ValidatePathUseCase() }

    val simulateFlockRunUseCase: SimulateFlockRunUseCase by lazy { SimulateFlockRunUseCase() }

    val awardMedalUseCase: AwardMedalUseCase by lazy { AwardMedalUseCase() }

    val saveProgressUseCase: SaveProgressUseCase by lazy { SaveProgressUseCase(progressRepository) }

    private fun requireContext(): Context =
        requireNotNull(appContext) { "ServiceLocator.init must run before use" }
}
