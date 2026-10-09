package com.farmfeather.bridge.presentation.menu

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.farmfeather.bridge.MainActivity
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.util.ScoreFormatter
import com.farmfeather.bridge.databinding.FragmentMenuBinding
import com.farmfeather.bridge.databinding.ItemMissionChipBinding
import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.presentation.common.ViewModelFactory
import com.farmfeather.bridge.presentation.dialog.MedalBarnDialog
import com.farmfeather.bridge.presentation.dialog.SettingsDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var binding: FragmentMenuBinding? = null

    private val viewModel: MenuViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val views = binding ?: return

        views.btnPlay.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showGame(viewModel.uiState.value.selectedMission)
        }
        views.btnMedalBarn.setOnClickListener {
            MedalBarnDialog().show(parentFragmentManager, MedalBarnDialog.TAG)
        }
        views.btnSettings.setOnClickListener {
            SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
        }

        animateEntrance(views)
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: MenuUiState) {
        val views = binding ?: return
        val context = views.root.context

        views.txtMenuTagline.setText(
            if (state.isFresh) R.string.menu_tagline_fresh else R.string.menu_tagline
        )

        renderChips(state)

        views.rowMenuStats.visibility = if (state.showStats) View.VISIBLE else View.GONE
        if (state.showStats) {
            views.statMenuMedals.visibility =
                if (state.medalCount > 0) View.VISIBLE else View.INVISIBLE
            views.statMenuMedals.bind(
                ScoreFormatter.ratio(state.medalCount, state.totalMissions),
                getString(R.string.stat_medals),
                ContextCompat.getColor(context, R.color.farm_secondary)
            )
            views.statMenuCoins.visibility = if (state.coins > 0) View.VISIBLE else View.INVISIBLE
            views.statMenuCoins.bind(
                ScoreFormatter.score(state.coins),
                getString(R.string.stat_coins),
                ContextCompat.getColor(context, R.color.farm_primary)
            )
            views.statMenuBest.visibility =
                if (state.bestMedal != Medal.NONE) View.VISIBLE else View.INVISIBLE
            views.statMenuBest.bind(
                getString(medalLabel(state.bestMedal)),
                getString(R.string.stat_best),
                ContextCompat.getColor(context, R.color.farm_accent)
            )
        }
    }

    private fun renderChips(state: MenuUiState) {
        val views = binding ?: return
        val container = views.containerMissions
        container.removeAllViews()
        val context = container.context
        val unlockedTint = ContextCompat.getColor(context, R.color.farm_accent)
        val currentTint = ContextCompat.getColor(context, R.color.farm_primary)
        val lockedTint = ContextCompat.getColor(context, R.color.farm_bg_deep)
        val lockedText = ContextCompat.getColor(context, R.color.farm_text_faint)
        val activeText = ContextCompat.getColor(context, R.color.farm_text_inverse)

        state.missions.forEach { mission ->
            val chipBinding = ItemMissionChipBinding.inflate(layoutInflater, container, false)
            val chip = chipBinding.btnMissionChip
            val unlocked = mission.id <= state.unlockedMission
            val current = mission.id == state.selectedMission
            chip.text = mission.id.toString()
            chip.isEnabled = unlocked
            chip.alpha = if (unlocked) 1f else LOCKED_ALPHA
            chip.backgroundTintList = ColorStateList.valueOf(
                when {
                    current -> currentTint
                    unlocked -> unlockedTint
                    else -> lockedTint
                }
            )
            chip.setTextColor(if (unlocked) activeText else lockedText)
            chip.strokeWidth = if (current) currentStroke() else 0
            chip.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.farm_secondary)
            )
            val description = when {
                current -> getString(R.string.cd_mission_current, mission.id)
                unlocked -> getString(R.string.cd_mission_unlocked, mission.id)
                else -> getString(R.string.cd_mission_locked, mission.id)
            }
            chip.contentDescription = description
            ViewCompat.setStateDescription(chip, mission.name)
            if (unlocked) {
                chip.setOnClickListener { viewModel.selectMission(mission.id) }
            }
            container.addView(chip)
        }
    }

    private fun animateEntrance(views: FragmentMenuBinding) {
        views.cardMenuSheet.alpha = 0f
        views.cardMenuSheet.translationY = sheetOffset()
        views.cardMenuSheet.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(SHEET_ENTRY_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()

        views.imgMenuChicken.translationY = 0f
        views.imgMenuChicken.animate()
            .translationY(-heroBob())
            .setDuration(HERO_BOB_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun medalLabel(medal: Medal): Int = when (medal) {
        Medal.GOLD -> R.string.medal_gold
        Medal.SILVER -> R.string.medal_silver
        Medal.BRONZE -> R.string.medal_bronze
        Medal.NONE -> R.string.medal_none
    }

    private fun currentStroke(): Int = (resources.displayMetrics.density * CHIP_STROKE_DP).toInt()

    private fun sheetOffset(): Float = resources.displayMetrics.density * SHEET_OFFSET_DP

    private fun heroBob(): Float = resources.displayMetrics.density * HERO_BOB_DP

    override fun onDestroyView() {
        binding?.cardMenuSheet?.animate()?.cancel()
        binding?.imgMenuChicken?.animate()?.cancel()
        binding = null
        super.onDestroyView()
    }

    private companion object {
        const val LOCKED_ALPHA = 0.55f
        const val CHIP_STROKE_DP = 2f
        const val SHEET_OFFSET_DP = 40f
        const val SHEET_ENTRY_MS = 380L
        const val HERO_BOB_DP = 8f
        const val HERO_BOB_MS = 2200L
    }
}
