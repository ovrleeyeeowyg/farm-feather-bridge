package com.farmfeather.bridge.presentation.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.farmfeather.bridge.core.di.ServiceLocator
import com.farmfeather.bridge.databinding.DialogMedalBarnBinding
import com.farmfeather.bridge.domain.model.Medal

class MedalBarnDialog : DialogFragment() {

    private var binding: DialogMedalBarnBinding? = null
    private val adapter = MedalRowAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogMedalBarnBinding.inflate(inflater, container, false)
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
        views.listMedals.layoutManager = LinearLayoutManager(views.root.context)
        views.listMedals.adapter = adapter
        views.btnCloseMedalBarn.setOnClickListener { dismissAllowingStateLoss() }
        renderRows()
    }

    private fun renderRows() {
        val views = binding ?: return
        val progress = ServiceLocator.progressRepository.load()
        val rows = ServiceLocator.missionRepository.all().map { mission ->
            MedalRowItem(
                missionId = mission.id,
                missionName = mission.name,
                medal = progress.medals[mission.id] ?: Medal.NONE,
                bestScore = progress.missionScores[mission.id] ?: 0,
                unlocked = mission.id <= progress.unlockedMission
            )
        }
        adapter.submit(rows)
        val empty = progress.medalCount == 0
        views.emptyMedalBarn.visibility = if (empty) View.VISIBLE else View.GONE
        views.listMedals.visibility = if (empty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        binding?.listMedals?.adapter = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "dialog_medal_barn"
    }
}
