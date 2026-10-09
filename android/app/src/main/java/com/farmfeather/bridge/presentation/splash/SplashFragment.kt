package com.farmfeather.bridge.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.farmfeather.bridge.MainActivity
import com.farmfeather.bridge.R
import com.farmfeather.bridge.databinding.FragmentSplashBinding
import com.farmfeather.bridge.presentation.common.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null

    private val viewModel: SplashViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val views = binding ?: return
        val splashAnimator = SplashAnimator(
            medallion = views.splashMedallion,
            title = views.txtSplashTitle,
            subtitle = views.txtSplashSubtitle,
            statusLine = views.txtSplashStatus,
            ghosts = listOf(views.ghostPlankOne, views.ghostPlankTwo, views.ghostPlankThree)
        )
        animator = splashAnimator
        splashAnimator.prepare()
        splashAnimator.start()

        views.progressSplash.isIndeterminate = true
        views.txtSplashStatus.text = getString(statusFor(0))

        observeStage()
        observeProgress()
        observeNavigation()
        viewModel.start()
    }

    private fun observeStage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stage.collectLatest { stage ->
                    val views = binding ?: return@collectLatest
                    views.txtSplashStatus.text = getString(statusFor(stage))
                }
            }
        }
    }

    private fun observeProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collectLatest { percent ->
                    val views = binding ?: return@collectLatest
                    views.progressSplash.contentDescription =
                        getString(R.string.splash_subtitle) + " " + percent
                }
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigate.collectLatest { shouldNavigate ->
                    if (!shouldNavigate) return@collectLatest
                    if (!isAdded || view == null) return@collectLatest
                    val host = activity as? MainActivity ?: return@collectLatest
                    if (!host.navigator.showMenu()) return@collectLatest
                    viewModel.onNavigated()
                }
            }
        }
    }

    private fun statusFor(stage: Int): Int = when (stage) {
        0 -> R.string.splash_status_waking
        1 -> R.string.splash_status_creek
        2 -> R.string.splash_status_planks
        else -> R.string.splash_status_ready
    }

    override fun onDestroyView() {
        animator?.cancel()
        animator = null
        binding = null
        super.onDestroyView()
    }
}
