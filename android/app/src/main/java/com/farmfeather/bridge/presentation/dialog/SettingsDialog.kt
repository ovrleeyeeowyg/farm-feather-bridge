package com.farmfeather.bridge.presentation.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.di.ServiceLocator
import com.farmfeather.bridge.databinding.DialogSettingsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsDialog : DialogFragment() {

    private var binding: DialogSettingsBinding? = null
    private var confirmingReset = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogSettingsBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val views = binding ?: return
        val repository = ServiceLocator.progressRepository

        views.switchSound.isChecked = repository.isSoundEnabled()
        views.switchCartFlash.isChecked = repository.isCartFlashEnabled()
        views.switchLargeLabels.isChecked = repository.isLargeLabelsEnabled()
        applyState(views.switchSound, views.switchSound.isChecked)
        applyState(views.switchCartFlash, views.switchCartFlash.isChecked)
        applyState(views.switchLargeLabels, views.switchLargeLabels.isChecked)

        views.switchSound.setOnCheckedChangeListener { button, checked ->
            repository.setSoundEnabled(checked)
            applyState(button, checked)
        }
        views.switchCartFlash.setOnCheckedChangeListener { button, checked ->
            repository.setCartFlashEnabled(checked)
            applyState(button, checked)
        }
        views.switchLargeLabels.setOnCheckedChangeListener { button, checked ->
            repository.setLargeLabelsEnabled(checked)
            applyState(button, checked)
        }

        views.btnResetProgress.setOnClickListener { onResetTapped() }
        views.btnCloseSettings.setOnClickListener { dismissAllowingStateLoss() }
    }

    private fun onResetTapped() {
        val views = binding ?: return
        if (!confirmingReset) {
            confirmingReset = true
            views.btnResetProgress.setText(R.string.action_reset_confirm)
            startConfirmWindow()
            return
        }
        confirmingReset = false
        ServiceLocator.progressRepository.reset()
        views.btnResetProgress.setText(R.string.action_reset_progress)
        dismissAllowingStateLoss()
    }

    private fun startConfirmWindow() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(CONFIRM_WINDOW_MS)
            val views = binding ?: return@launch
            if (!isAdded) return@launch
            if (!confirmingReset) return@launch
            confirmingReset = false
            views.btnResetProgress.setText(R.string.action_reset_progress)
        }
    }

    private fun applyState(button: CompoundButton, checked: Boolean) {
        val label = getString(if (checked) R.string.settings_state_on else R.string.settings_state_off)
        ViewCompat.setStateDescription(button, label)
        button.alpha = if (checked) 1f else INACTIVE_ALPHA
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "dialog_settings"
        private const val CONFIRM_WINDOW_MS = 3000L
        private const val INACTIVE_ALPHA = 0.75f
    }
}
