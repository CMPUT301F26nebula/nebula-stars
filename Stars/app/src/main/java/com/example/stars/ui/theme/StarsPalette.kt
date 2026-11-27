package com.example.stars.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.stars.ui.theme.ThemePresets.Default
import com.example.stars.ui.theme.ThemePresets.all

/**
 * App colors, each val has one job.
 */
data class StarsPalette(
    val name: String,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val accent1: Color,
    val accent2: Color,
    val accent3: Color,
    val onAccent: Color,
    val highlight: Color,
    val onHighlight: Color,
    val muted: Color,
    val confirm: Color = Color.Green,
    val cancel: Color = Color.Red
)

/**
 * The built in themes the user can pick from settings
 */
object ThemePresets {
    //mockup colors/default theme
    val Default = StarsPalette(
        name = "Default",
        background = Color(0xFF4B0082),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF4B0082),
        accent1 = Color(0xFF008B8B),
        accent2 = Color(0xFFFF1493),
        accent3 = Color(0xFFEFC700),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFFB758E8),
        onHighlight = Color(0xFFFFFFFF),
        muted = Color(0xFF800080),
        confirm = Color(0xFFFF1493),
        cancel = Color(0xFF008B8B),
    )

    // light theme
    val Light = StarsPalette(
        name = "Light",
        background = Color(0xFFF4EFFA),
        onBackground = Color(0xFF2A1745),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF4B0082),
        accent1 = Color(0xFF00796B),
        accent2 = Color(0xFFC2185B),
        accent3 = Color(0xFF9A6F00),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFF7E57C2),
        onHighlight = Color(0xFFFFFFFF),
        muted = Color(0xFFD9CCEB),
        confirm = Color(0xFFC2185B),
        cancel = Color(0xFF00796B)
    )

    // dark theme
    val Dark = StarsPalette(
        name = "Dark",
        background = Color(0xFF121212),
        onBackground = Color(0xFFE6E1EC),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFD0BCFF),
        accent1 = Color(0xFF1F5C57),
        accent2 = Color(0xFF7A2048),
        accent3 = Color(0xFF6B5A1E),
        onAccent = Color(0xFFF2EEF8),
        highlight = Color(0xFFD0BCFF),
        onHighlight = Color(0xFF381E72),
        muted = Color(0xFF2B2930),
        confirm = Color(0xFF7A2048),
        cancel = Color(0xFF1F5C57)
    )

    // dark purple theme
    val Nebula = StarsPalette(
        name = "Nebula",
        background = Color(0xFF1A0B2E),
        onBackground = Color(0xFFF3E8FF),
        surface = Color(0xFF2D1B4E),
        onSurface = Color(0xFFE0C3FC),
        accent1 = Color(0xFF3C096C),
        accent2 = Color(0xFF7B2CBF),
        accent3 = Color(0xFF9D4EDD),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFFC77DFF),
        onHighlight = Color(0xFF1A0B2E),
        muted = Color(0xFF3C2A5C)
    )

    // dark blue/pale yellow theme
    val Moonlight = StarsPalette(
        name = "Moonlight",
        background = Color(0xFF0B1A33),
        onBackground = Color(0xFFFFF6D5),
        surface = Color(0xFF13294B),
        onSurface = Color(0xFFF6E7A1),
        accent1 = Color(0xFF1E3A64),
        accent2 = Color(0xFF2F5A8F),
        accent3 = Color(0xFF3D6FA8),
        onAccent = Color(0xFFFFF6D5),
        highlight = Color(0xFFF6E7A1),
        onHighlight = Color(0xFF0B1A33),
        muted = Color(0xFF1B2F52)
    )

    // dark green theme
    val Forest = StarsPalette(
        name = "Forest",
        background = Color(0xFF0F2417),
        onBackground = Color(0xFFEEF5E9),
        surface = Color(0xFF1B3A26),
        onSurface = Color(0xFFCDE8C4),
        accent1 = Color(0xFF1B4332),
        accent2 = Color(0xFF2D6A4F),
        accent3 = Color(0xFF40916C),
        onAccent = Color(0xFFF1FAEE),
        highlight = Color(0xFF95D5B2),
        onHighlight = Color(0xFF0F2417),
        muted = Color(0xFF22412F)
    )

    val all = listOf(Default, Light, Dark, Nebula, Moonlight, Forest)

    /**
     * Finds a preset by its saved name, or Default if it doesnt exist
     */
    fun byName(name: String): StarsPalette = all.find { it.name == name } ?: Default
}