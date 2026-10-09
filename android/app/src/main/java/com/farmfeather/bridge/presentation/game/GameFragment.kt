package com.farmfeather.bridge.presentation.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.farmfeather.bridge.MainActivity
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.config.GameConfig
import com.farmfeather.bridge.core.util.ScoreFormatter
import com.farmfeather.bridge.databinding.FragmentGameBinding
import com.farmfeather.bridge.presentation.common.ViewModelFactory
import com.farmfeather.bridge.presentation.gameover.GameOverFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var binding: FragmentGameBinding? = null
    private var tickAnimator: ValueAnimator? = null

    private val viewModel: GameViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val views = binding ?: return

        views.btnGameBack.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.goBack()
        }
        views.btnSendFlock.setOnClickListener { viewModel.sendFlock() }
        views.piecePicker.onSelect = { kind -> viewModel.selectPiece(kind) }
        views.bridgeBoard.onCellTap = { row, col -> viewModel.onCellTap(row, col) }
        views.bridgeBoard.onCellLongPress = { row, col -> viewModel.onCellLongPress(row, col) }
        views.bridgeBoard.onRunFinished = { viewModel.onRunFinished() }
        views.statGameCart.bind(
            ScoreFormatter.cartPace(GameConfig.CART_PERIOD_MS),
            getString(R.string.stat_cart),
            ContextCompat.getColor(views.root.context, R.color.farm_secondary)
        )
        views.statGameFlock.setCompact(true)
        views.statGamePlanks.setCompact(true)
        views.statGameCart.setCompact(true)

        observeState()
        observeRunCommand()
        observePop()
        observeOutcome()
        viewModel.start(missionId())
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeOrRestart()
    }

    private fun syncTicker() {
        val views = binding ?: return
        if (views.bridgeBoard.needsAnimation()) startTicker() else stopTicker()
    }

    private fun startTicker() {
        if (tickAnimator != null) return
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = TICKER_CYCLE_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            try {
                if (!isAdded) return@addUpdateListener
                val board = binding?.bridgeBoard ?: return@addUpdateListener
                board.onTick()
                if (!board.needsAnimation()) stopTicker()
            } catch (e: Exception) {
            }
        }
        tickAnimator = animator
        animator.start()
    }

    private fun stopTicker() {
        tickAnimator?.cancel()
        tickAnimator = null
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun observeRunCommand() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.runCommand.collectLatest { command ->
                    val views = binding ?: return@collectLatest
                    command ?: return@collectLatest
                    views.bridgeBoard.startRun(
                        command.path,
                        command.plans,
                        command.startUptime,
                        command.durationMs
                    )
                    syncTicker()
                    viewModel.onRunConsumed()
                }
            }
        }
    }

    private fun observePop() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.popCell.collectLatest { key ->
                    val views = binding ?: return@collectLatest
                    if (key < 0) return@collectLatest
                    views.bridgeBoard.popCell(key)
                    syncTicker()
                    viewModel.onPopConsumed()
                }
            }
        }
    }

    private fun observeOutcome() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.outcome.collectLatest { outcome ->
                    outcome ?: return@collectLatest
                    if (!isAdded || view == null) return@collectLatest
                    val host = activity as? MainActivity ?: return@collectLatest
                    val shown = host.navigator.showGameOver(GameOverFragment.argsOf(outcome))
                    if (!shown) return@collectLatest
                    viewModel.onOutcomeConsumed()
                }
            }
        }
    }

    private fun render(state: GameUiState) {
        val views = binding ?: return
        val context = views.root.context

        views.txtMissionLabel.text = getString(R.string.mission_label, state.missionId)
        views.txtMissionName.text = state.missionName
        views.txtGameTimer.text = ScoreFormatter.clock(state.remainingMs)
        views.txtGameTimer.setTextColor(
            ContextCompat.getColor(
                context,
                if (state.urgent) R.color.farm_danger else R.color.farm_primary
            )
        )

        views.bridgeBoard.setBoard(state.board)
        views.bridgeBoard.setPieces(state.pieces)
        views.bridgeBoard.setRoute(state.routeCells, state.routeReady)
        syncTicker()

        views.piecePicker.bind(
            state.straightLeft,
            state.cornerLeft,
            state.selected,
            state.inputEnabled
        )

        views.statGameFlock.bind(
            ScoreFormatter.ratio(state.flockSaved, GameConfig.FLOCK_SIZE),
            getString(R.string.stat_flock),
            ContextCompat.getColor(context, R.color.farm_accent)
        )
        views.statGamePlanks.bind(
            state.planksLeft.toString(),
            getString(R.string.stat_planks),
            ContextCompat.getColor(context, R.color.farm_primary)
        )
        views.statGamePlanks.visibility =
            if (state.planksLeft > 0) View.VISIBLE else View.INVISIBLE

        val ready = state.routeReady && state.inputEnabled
        views.btnSendFlock.isEnabled = ready
        views.btnSendFlock.alpha = if (ready) 1f else DISABLED_ALPHA
        views.btnSendFlock.contentDescription = getString(
            if (ready) R.string.cd_send_ready else R.string.cd_send_locked
        )
        ViewCompat.setStateDescription(
            views.btnSendFlock,
            getString(if (ready) R.string.cd_send_ready else R.string.cd_send_locked)
        )

        views.txtGameHint.visibility =
            if (state.phase == GamePhase.RUNNING) View.INVISIBLE else View.VISIBLE
        views.viewGameDim.visibility =
            if (state.phase == GamePhase.RESULT) View.VISIBLE else View.GONE
    }

    private fun missionId(): Int = arguments?.getInt(ARG_MISSION_ID, 1) ?: 1

    override fun onDestroyView() {
        stopTicker()
        binding?.bridgeBoard?.stopRun()
        binding?.bridgeBoard?.onRunFinished = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_MISSION_ID = "arg_mission_id"
        private const val TICKER_CYCLE_MS = 1000L
        private const val DISABLED_ALPHA = 0.5f

        fun newInstance(missionId: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putInt(ARG_MISSION_ID, missionId)
            fragment.arguments = args
            return fragment
        }
    }
}
