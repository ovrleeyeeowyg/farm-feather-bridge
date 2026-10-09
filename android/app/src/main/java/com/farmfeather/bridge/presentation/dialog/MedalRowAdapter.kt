package com.farmfeather.bridge.presentation.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.farmfeather.bridge.R
import com.farmfeather.bridge.core.util.ScoreFormatter
import com.farmfeather.bridge.databinding.ItemMedalRowBinding

class MedalRowAdapter : RecyclerView.Adapter<MedalRowAdapter.MedalViewHolder>() {

    private val items = ArrayList<MedalRowItem>()

    fun submit(rows: List<MedalRowItem>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedalViewHolder {
        val binding = ItemMedalRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MedalViewHolder(
        private val binding: ItemMedalRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedalRowItem) {
            val context = binding.root.context
            binding.rowMedalRibbon.bind(item.medal, true)
            binding.txtRowMissionName.text = item.missionName
            binding.txtRowMissionScore.text = if (item.unlocked && item.bestScore > 0) {
                context.getString(R.string.medal_row_best, ScoreFormatter.score(item.bestScore))
            } else {
                context.getString(R.string.medal_row_locked)
            }
            binding.root.alpha = if (item.unlocked) 1f else LOCKED_ALPHA
            binding.root.contentDescription = item.missionName
        }

        private companion object {
            const val LOCKED_ALPHA = 0.6f
        }
    }
}
