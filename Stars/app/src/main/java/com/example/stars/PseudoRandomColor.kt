/**
 * Generates a random colour from a unique id so a colour is consistent per unique item
 */
package com.example.stars

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Maps a unique id to a set of colours
 *
 * @param colors list of potential colours
 * @param contrastColor map of colours in [colors] to their contrast colour
 */
class PseudoRandomColor(
    private val colors: List<Color> = listOf(
        StarsColors.Cyan,
        StarsColors.Magenta,
        StarsColors.Yellow
    ),
    private val contrastColor: Map<Color, Color> = mapOf(
        StarsColors.Cyan to StarsColors.White,
        StarsColors.Magenta to StarsColors.White,
        StarsColors.Yellow to StarsColors.White
    )
) {
    /**
     * Get a [Color] from an [id] string
     */
    fun fromId(id: String?): Color {
        return colors[abs(id.hashCode() % colors.size)]
    }

    /**
     * Gets a [color]s [contrastColor]
     */
    fun contrast(color: Color): Color {
        return contrastColor.getOrDefault(color, StarsColors.Black)
    }
}