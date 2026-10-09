package com.farmfeather.bridge.presentation.gameover

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.farmfeather.bridge.MainActivity
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.core.util.ScoreFormatter
import com.farmfeather.bridge.databinding.FragmentGameOverBinding
import com.farmfeather.bridge.domain.model.Medal
import com.farmfeather.bridge.domain.model.MissionOutcome
import com.farmfeather.bridge.presentation.common.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var binding: FragmentGameOverBinding? = null
    private var scoreAnimator: ValueAnimator? = null

    private val viewModel: GameOverViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameOverBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val views = binding ?: return

        views.btnPlayAgain.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showGame(viewModel.uiState.value.missionId)
        }
        views.btnNextMission.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showGame(viewModel.nextMissionId())
        }
        views.btnBackToMenu.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showMenu()
        }
        views.btnRetrySave.setOnClickListener { viewModel.retrySave() }

        viewModel.load(stateFromArguments())
        observeState()
        animateMedal(views)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: GameOverUiState) {
        val views = binding ?: return
        val context = views.root.context

        views.txtResultTitle.setText(titleFor(state.medal))
        views.txtResultTitle.setTextColor(ContextCompat.getColor(context, titleColor(state.medal)))
        views.txtResultMission.text = state.missionName

        views.medalRibbon.bind(state.medal, false)

        views.statResultSaved.bind(
            ScoreFormatter.ratio(state.saved, state.flockSize),
            getString(R.string.stat_saved),
            ContextCompat.getColor(context, R.color.farm_accent)
        )
        views.statResultPlanks.bind(
            state.planksLeft.toString(),
            getString(R.string.stat_planks_left),
            ContextCompat.getColor(context, R.color.farm_secondary)
        )
        views.statResultPlanks.visibility = if (state.showPlanks) View.VISIBLE else View.INVISIBLE
        views.statResultTime.bind(
            ScoreFormatter.seconds(state.secondsLeft),
            getString(R.string.stat_time),
            ContextCompat.getColor(context, R.color.farm_wood)
        )
        views.statResultScore.bind(
            ScoreFormatter.score(state.score),
            getString(R.string.stat_score),
            ContextCompat.getColor(context, R.color.farm_primary)
        )
        animateScore(state.score)

        views.txtResultCoins.text = getString(R.string.result_coins, state.coins)
        views.txtResultCoins.visibility = if (state.showCoins) View.VISIBLE else View.GONE
        views.txtResultCoinsNote.visibility = if (state.showCoins) View.VISIBLE else View.GONE

        views.btnNextMission.visibility = if (state.unlockedNext) View.VISIBLE else View.GONE
        views.txtResultSaveError.visibility = if (state.saveFailed) View.VISIBLE else View.GONE
        views.btnRetrySave.visibility = if (state.saveFailed) View.VISIBLE else View.GONE
    }

    private fun animateScore(target: Int) {
        val views = binding ?: return
        scoreAnimator?.cancel()
        if (target <= 0) return
        val animator = ValueAnimator.ofInt(0, target)
        animator.duration = SCORE_COUNT_MS
        animator.addUpdateListener { update ->
            try {
                if (!isAdded) return@addUpdateListener
                val current = binding ?: return@addUpdateListener
                val value = update.animatedValue as Int
                current.statResultScore.bind(
                    ScoreFormatter.score(value),
                    getString(R.string.stat_score),
                    ContextCompat.getColor(views.root.context, R.color.farm_primary)
                )
            } catch (e: Exception) {
            }
        }
        scoreAnimator = animator
        animator.start()
    }

    private fun animateMedal(views: FragmentGameOverBinding) {
        views.medalRibbon.scaleX = MEDAL_START_SCALE
        views.medalRibbon.scaleY = MEDAL_START_SCALE
        views.medalRibbon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(MEDAL_REVEAL_MS)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()
    }

    private fun titleFor(medal: Medal): Int = when (medal) {
        Medal.GOLD -> R.string.result_gold
        Medal.SILVER -> R.string.result_silver
        Medal.BRONZE -> R.string.result_bronze
        Medal.NONE -> R.string.result_none
    }

    private fun titleColor(medal: Medal): Int = when (medal) {
        Medal.GOLD -> R.color.farm_primary
        Medal.SILVER -> R.color.farm_accent
        Medal.BRONZE -> R.color.farm_wood
        Medal.NONE -> R.color.farm_danger
    }

    private fun stateFromArguments(): GameOverUiState {
        val args = arguments ?: return GameOverUiState()
        return GameOverUiState(
            missionId = args.getInt(ARG_MISSION_ID, 1),
            missionName = args.getString(ARG_MISSION_NAME).orEmpty(),
            medal = Medal.fromName(args.getString(ARG_MEDAL).orEmpty()),
            score = args.getInt(ARG_SCORE, 0),
            coins = args.getInt(ARG_COINS, 0),
            saved = args.getInt(ARG_SAVED, 0),
            flockSize = args.getInt(ARG_FLOCK_SIZE, GameConfig.FLOCK_SIZE),
            planksLeft = args.getInt(ARG_PLANKS_LEFT, 0),
            secondsLeft = args.getInt(ARG_SECONDS_LEFT, 0),
            unlockedNext = args.getBoolean(ARG_UNLOCKED_NEXT, false),
            saveFailed = args.getBoolean(ARG_SAVE_FAILED, false)
        )
    }

    override fun onDestroyView() {
        scoreAnimator?.cancel()
        scoreAnimator = null
        binding?.medalRibbon?.animate()?.cancel()
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_MISSION_ID = "arg_mission_id"
        private const val ARG_MISSION_NAME = "arg_mission_name"
        private const val ARG_MEDAL = "arg_medal"
        private const val ARG_SCORE = "arg_score"
        private const val ARG_COINS = "arg_coins"
        private const val ARG_SAVED = "arg_saved"
        private const val ARG_FLOCK_SIZE = "arg_flock_size"
        private const val ARG_PLANKS_LEFT = "arg_planks_left"
        private const val ARG_SECONDS_LEFT = "arg_seconds_left"
        private const val ARG_UNLOCKED_NEXT = "arg_unlocked_next"
        private const val ARG_SAVE_FAILED = "arg_save_failed"
        private const val SCORE_COUNT_MS = 700L
        private const val MEDAL_REVEAL_MS = 520L
        private const val MEDAL_START_SCALE = 0.6f

        fun argsOf(outcome: MissionOutcome): Bundle {
            val args = Bundle()
            args.putInt(ARG_MISSION_ID, outcome.missionId)
            args.putString(ARG_MISSION_NAME, outcome.missionName)
            args.putString(ARG_MEDAL, outcome.medal.name)
            args.putInt(ARG_SCORE, outcome.score)
            args.putInt(ARG_COINS, outcome.coins)
            args.putInt(ARG_SAVED, outcome.saved)
            args.putInt(ARG_FLOCK_SIZE, outcome.flockSize)
            args.putInt(ARG_PLANKS_LEFT, outcome.planksLeft)
            args.putInt(ARG_SECONDS_LEFT, outcome.secondsLeft)
            args.putBoolean(ARG_UNLOCKED_NEXT, outcome.unlockedNext)
            args.putBoolean(ARG_SAVE_FAILED, outcome.saveFailed)
            return args
        }

        fun newInstance(args: Bundle): GameOverFragment {
            val fragment = GameOverFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
