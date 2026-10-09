package com.farmfeather.bridge.domain.repository

import com.farmfeather.bridge.domain.model.Mission

interface MissionRepository {
    fun all(): List<Mission>
    fun byId(id: Int): Mission
    fun count(): Int
}
