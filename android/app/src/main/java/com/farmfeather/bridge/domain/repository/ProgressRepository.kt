package com.farmfeather.bridge.domain.repository

import com.farmfeather.bridge.domain.model.PlayerProgress

interface ProgressRepository {
    fun load(): PlayerProgress
    fun save(progress: PlayerProgress): Boolean
    fun reset(): Boolean
    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)
    fun isCartFlashEnabled(): Boolean
    fun setCartFlashEnabled(enabled: Boolean)
    fun isLargeLabelsEnabled(): Boolean
    fun setLargeLabelsEnabled(enabled: Boolean)
}
