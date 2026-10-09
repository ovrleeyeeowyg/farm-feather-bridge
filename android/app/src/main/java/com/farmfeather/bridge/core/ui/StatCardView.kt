package com.farmfeather.bridge.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.farmfeather.bridge.R
import com.farmfeather.bridge.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    private var compact = false

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.shape_stat_card)
        val padding = resources.getDimensionPixelSize(R.dimen.stat_card_padding)
        setPadding(padding, padding, padding, padding)
        bind("", "", ContextCompat.getColor(context, R.color.farm_accent))
    }

    fun bind(value: String, label: String, accentColor: Int) {
        binding.statValue.text = value
        binding.statValue.setTextColor(accentColor)
        binding.statLabel.text = label
        ViewCompat.setBackgroundTintList(
            binding.statAccentDot,
            ColorStateList.valueOf(accentColor)
        )
        ViewCompat.setStateDescription(this, label)
        contentDescription = "$label $value"
    }

    fun setCompact(value: Boolean) {
        if (compact == value) return
        compact = value
        val valueSize = if (compact) COMPACT_VALUE_SP else REGULAR_VALUE_SP
        binding.statValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, valueSize)
        binding.statAccentDot.visibility = if (compact) GONE else VISIBLE
        if (compact) {
            setBackgroundResource(0)
            setPadding(0, 0, 0, 0)
        }
    }

    private companion object {
        const val REGULAR_VALUE_SP = 22f
        const val COMPACT_VALUE_SP = 17f
    }
}
