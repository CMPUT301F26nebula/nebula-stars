/**
 * Generates a random colour from a unique id so a colour is consistent per unique item
 */
package com.example.stars

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Maps a unique id to a set of colors
 *
 * @param colors list of potential colors
 * @param contrastColor map of colors in [colors] to their contrast color
 */
class PseudoRandomColor {
    /**
     * The accent colors of the current theme
     */
    private val colors: List<Color>
        get() = listOf(
            StarsColors.Accent1,
            StarsColors.Accent2,
            StarsColors.Accent3
        )

    /**
     * Get a [Color] from an [id] string
     */
    fun fromId(id: String?): Color {
        return colors[abs(id.hashCode() % colors.size)]
    }

    /**
     * Gets the text color to use on top of [color]
     */
    fun contrast(color: Color): Color {
        return if (color in colors) StarsColors.OnAccent else StarsColors.Black
    }

    /**
     * Generates [ButtonColors] from a users [id]
     */
    fun buttonColorsFromId(id: String): ButtonColors {
        val container = fromId(id)
        val content = contrast(container)
        return ButtonColors(
            container,
            content,
            container,
            content
        )
    }

    /**
     * Generates [CardColors] from a users [id]
     */
    fun cardColorsFromId(id: String): CardColors {
        val container = fromId(id)
        val content = contrast(container)
        return CardColors(
            container,
            content,
            container,
            content
        )
    }
}