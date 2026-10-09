package com.farmfeather.bridge.presentation.splash

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator

class SplashAnimator(
    private val medallion: View,
    private val title: View,
    private val subtitle: View,
    private val statusLine: View,
    private val ghosts: List<View>
) {

    private val animators = ArrayList<Animator>()
    private var started = false

    fun prepare() {
        medallion.alpha = 0f
        medallion.scaleX = ENTRY_SCALE
        medallion.scaleY = ENTRY_SCALE
        title.alpha = 0f
        title.translationY = titleOffset()
        subtitle.alpha = 0f
        statusLine.alpha = 0f
        ghosts.forEach { ghost ->
            ghost.alpha = 0f
        }
    }

    fun start() {
        if (started) return
        started = true
        animators.add(buildMedallionEntry())
        animators.add(buildTitleEntry())
        animators.add(buildSubtitleEntry())
        animators.add(buildStatusEntry())
        animators.add(buildMedallionSway())
        ghosts.forEachIndexed { index, ghost ->
            animators.add(buildGhostFade(ghost, index))
            animators.add(buildGhostDrift(ghost, index))
        }
        animators.forEach { it.start() }
    }

    fun cancel() {
        animators.forEach { it.cancel() }
        animators.clear()
        medallion.animate().cancel()
        title.animate().cancel()
        subtitle.animate().cancel()
        statusLine.animate().cancel()
        ghosts.forEach { it.animate().cancel() }
        started = false
    }

    private fun buildMedallionEntry(): Animator {
        val fade = ObjectAnimator.ofFloat(medallion, View.ALPHA, 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(medallion, View.SCALE_X, ENTRY_SCALE, 1f)
        val scaleY = ObjectAnimator.ofFloat(medallion, View.SCALE_Y, ENTRY_SCALE, 1f)
        val set = AnimatorSet()
        set.playTogether(fade, scaleX, scaleY)
        set.duration = MEDALLION_ENTRY_MS
        set.interpolator = OvershootInterpolator(1.1f)
        return set
    }

    private fun buildTitleEntry(): Animator {
        val fade = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f)
        val slide = ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, titleOffset(), 0f)
        val set = AnimatorSet()
        set.playTogether(fade, slide)
        set.duration = TITLE_ENTRY_MS
        set.startDelay = TITLE_DELAY_MS
        set.interpolator = DecelerateInterpolator()
        return set
    }

    private fun buildSubtitleEntry(): Animator {
        val fade = ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f)
        fade.duration = SUBTITLE_ENTRY_MS
        fade.startDelay = SUBTITLE_DELAY_MS
        fade.interpolator = DecelerateInterpolator()
        return fade
    }

    private fun buildStatusEntry(): Animator {
        val fade = ObjectAnimator.ofFloat(statusLine, View.ALPHA, 0f, 1f)
        fade.duration = SUBTITLE_ENTRY_MS
        fade.startDelay = STATUS_DELAY_MS
        fade.interpolator = DecelerateInterpolator()
        return fade
    }

    private fun buildMedallionSway(): Animator {
        val sway = ObjectAnimator.ofFloat(
            medallion,
            View.ROTATION,
            0f,
            -SWAY_DEGREES,
            SWAY_DEGREES,
            0f
        )
        sway.duration = SWAY_MS
        sway.startDelay = MEDALLION_ENTRY_MS
        sway.repeatCount = SWAY_REPEATS
        sway.interpolator = DecelerateInterpolator()
        return sway
    }

    private fun buildGhostFade(ghost: View, index: Int): Animator {
        val fade = ObjectAnimator.ofFloat(ghost, View.ALPHA, 0f, GHOST_ALPHA)
        fade.duration = GHOST_FADE_MS
        fade.startDelay = GHOST_FADE_STEP_MS * index
        fade.interpolator = LinearInterpolator()
        return fade
    }

    private fun buildGhostDrift(ghost: View, index: Int): Animator {
        val distance = ghost.resources.displayMetrics.density * GHOST_DRIFT_DP
        val direction = if (index % 2 == 0) 1f else -1f
        val drift = ObjectAnimator.ofFloat(
            ghost,
            View.TRANSLATION_X,
            0f,
            -distance * direction,
            distance * direction,
            0f
        )
        drift.duration = GHOST_DRIFT_MS + index * GHOST_DRIFT_STEP_MS
        drift.repeatCount = GHOST_DRIFT_REPEATS
        drift.interpolator = DecelerateInterpolator()
        return drift
    }

    private fun titleOffset(): Float = title.resources.displayMetrics.density * TITLE_OFFSET_DP

    private companion object {
        const val ENTRY_SCALE = 0.72f
        const val MEDALLION_ENTRY_MS = 520L
        const val TITLE_ENTRY_MS = 420L
        const val TITLE_DELAY_MS = 220L
        const val SUBTITLE_ENTRY_MS = 360L
        const val SUBTITLE_DELAY_MS = 420L
        const val STATUS_DELAY_MS = 620L
        const val TITLE_OFFSET_DP = 28f
        const val SWAY_DEGREES = 3f
        const val SWAY_MS = 2600L
        const val SWAY_REPEATS = 1
        const val GHOST_ALPHA = 0.85f
        const val GHOST_FADE_MS = 700L
        const val GHOST_FADE_STEP_MS = 180L
        const val GHOST_DRIFT_DP = 10f
        const val GHOST_DRIFT_MS = 2800L
        const val GHOST_DRIFT_STEP_MS = 200L
        const val GHOST_DRIFT_REPEATS = 1
    }
}
