package com.farmfeather.bridge.domain.usecase

import com.farmfeather.bridge.domain.model.Mission
import com.farmfeather.bridge.domain.repository.MissionRepository

class LoadMissionUseCase(private val missionRepository: MissionRepository) {

    operator fun invoke(missionId: Int): Mission = missionRepository.byId(missionId)

    fun next(missionId: Int): Int {
        val total = missionRepository.count()
        return if (missionId >= total) total else missionId + 1
    }
}
