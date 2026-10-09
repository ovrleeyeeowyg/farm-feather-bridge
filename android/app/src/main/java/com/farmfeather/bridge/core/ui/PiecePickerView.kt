package com.farmfeather.bridge.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.farmfeather.bridge.R
import com.farmfeather.bridge.databinding.ViewPiecePickerBinding
import com.farmfeather.bridge.domain.model.PieceKind

class PiecePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPiecePickerBinding.inflate(LayoutInflater.from(context), this)

    var onSelect: ((PieceKind) -> Unit)? = null

    private var selected: PieceKind = PieceKind.STRAIGHT

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        binding.tileStraight.setOnClickListener { onSelect?.invoke(PieceKind.STRAIGHT) }
        binding.tileCorner.setOnClickListener { onSelect?.invoke(PieceKind.CORNER) }
        bind(0, 0, PieceKind.STRAIGHT, true)
    }

    fun bind(straightLeft: Int, cornerLeft: Int, selection: PieceKind, enabled: Boolean) {
        selected = selection
        binding.badgeStraight.text = straightLeft.toString()
        binding.badgeCorner.text = cornerLeft.toString()

        applyTile(
            tile = binding.tileStraight,
            kind = PieceKind.STRAIGHT,
            available = straightLeft > 0 && enabled,
            description = context.getString(R.string.cd_piece_straight)
        )
        applyTile(
            tile = binding.tileCorner,
            kind = PieceKind.CORNER,
            available = cornerLeft > 0 && enabled,
            description = context.getString(R.string.cd_piece_corner)
        )
    }

    private fun applyTile(tile: View, kind: PieceKind, available: Boolean, description: String) {
        val isSelected = kind == selected
        tile.setBackgroundResource(
            if (isSelected) R.drawable.shape_piece_tile_selected else R.drawable.shape_piece_tile
        )
        tile.alpha = if (available) 1f else DISABLED_ALPHA
        tile.isEnabled = available
        tile.contentDescription = description
        val state = if (isSelected) R.string.cd_selected else R.string.cd_not_selected
        ViewCompat.setStateDescription(tile, context.getString(state))
    }

    private companion object {
        const val DISABLED_ALPHA = 0.45f
    }
}
