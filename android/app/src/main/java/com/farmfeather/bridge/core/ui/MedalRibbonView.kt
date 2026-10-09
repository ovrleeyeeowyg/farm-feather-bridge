package com.farmfeather.bridge.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.farmfeather.bridge.R
import com.farmfeather.bridge.databinding.ViewMedalRibbonBinding
import com.farmfeather.bridge.domain.model.Medal

class MedalRibbonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMedalRibbonBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.shape_ribbon)
        val padding = resources.getDimensionPixelSize(R.dimen.space_s)
        setPadding(padding, padding, padding, padding)
        bind(Medal.NONE, false)
    }

    fun bind(medal: Medal, compact: Boolean) {
        val size = resources.getDimensionPixelSize(
            if (compact) R.dimen.medal_sprite_compact else R.dimen.medal_sprite_size
        )
        val params = binding.imgMedal.layoutParams
        params.width = size
        params.height = size
        binding.imgMedal.layoutParams = params

        val tint = ContextCompat.getColor(context, tintFor(medal))
        binding.imgMedal.imageTintList = ColorStateList.valueOf(tint)
        binding.imgMedal.alpha = if (medal == Medal.NONE) DIMMED_ALPHA else 1f

        val tierLabel = context.getString(labelFor(medal))
        binding.txtMedalTier.text = tierLabel
        binding.txtMedalTier.setTextColor(tint)
        binding.txtMedalTier.visibility = if (compact) GONE else VISIBLE

        val description = context.getString(R.string.cd_medal_tier, tierLabel)
        binding.imgMedal.contentDescription = description
        ViewCompat.setStateDescription(this, tierLabel)
    }

    private fun tintFor(medal: Medal): Int = when (medal) {
        Medal.GOLD -> R.color.farm_secondary
        Medal.SILVER -> R.color.farm_medal_silver
        Medal.BRONZE -> R.color.farm_medal_bronze
        Medal.NONE -> R.color.farm_medal_none
    }

    private fun labelFor(medal: Medal): Int = when (medal) {
        Medal.GOLD -> R.string.medal_gold
        Medal.SILVER -> R.string.medal_silver
        Medal.BRONZE -> R.string.medal_bronze
        Medal.NONE -> R.string.medal_none
    }

    private companion object {
        const val DIMMED_ALPHA = 0.45f
    }
}
