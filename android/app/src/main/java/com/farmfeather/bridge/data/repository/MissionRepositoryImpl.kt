package com.farmfeather.bridge.data.repository

import com.farmfeather.bridge.data.sample.SampleData
import com.farmfeather.bridge.domain.model.Mission
import com.farmfeather.bridge.domain.repository.MissionRepository

class MissionRepositoryImpl : MissionRepository {

    override fun all(): List<Mission> = SampleData.missions

    override fun byId(id: Int): Mission =
        SampleData.missions.firstOrNull { it.id == id } ?: SampleData.missions.first()

    override fun count(): Int = SampleData.missions.size
}
