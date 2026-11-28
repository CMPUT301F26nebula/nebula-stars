package com.example.stars.ui.theme

import androidx.compose.ui.graphics.Color

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
    //old mockup colors/legacy theme
    val Legacy = StarsPalette(
        name = "Legacy",
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
        confirm = Color(0xFF008B8B),
        cancel = Color(0xFFFF1493),
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
        muted = Color(0xFF3C2A5C),
        confirm = Color(0xFF752A96),
        cancel = Color(0xFF5C38B0)
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
        muted = Color(0xFF1B2F52),
        confirm = Color(0xFF330DB2),
        cancel = Color(0xFF0C42B4)
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
        muted = Color(0xFF22412F),
        confirm = Color(0xFF223D1B),
        cancel = Color(0xFF083304)
    )

    // dark and orange theme
    val Campfire = StarsPalette(
        name = "Campfire",
        background = Color(0xFF1C1412),
        onBackground = Color(0xFFF8EDE3),
        surface = Color(0xFF2A1E1A),
        onSurface = Color(0xFFFFC48C),
        accent1 = Color(0xFF5C2E1F),
        accent2 = Color(0xFF8C3B1E),
        accent3 = Color(0xFFB5541C),
        onAccent = Color(0xFFFFF4E6),
        highlight = Color(0xFFFF9F43),
        onHighlight = Color(0xFF1C1412),
        muted = Color(0xFF3A2A24),
        confirm = Color(0xFFB5541C),
        cancel = Color(0xFF5C4033)
    )

    // sandy theme
    val Dune = StarsPalette(
        name = "Dune",
        background = Color(0xFFFAF3E6),
        onBackground = Color(0xFF3B2F2F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF7A4E2D),
        accent1 = Color(0xFF7A4E2D),
        accent2 = Color(0xFFB0603A),
        accent3 = Color(0xFF8A7A2E),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFFD4A373),
        onHighlight = Color(0xFF3B2F2F),
        muted = Color(0xFFEADBC8),
        confirm = Color(0xFF2F6B5E),
        cancel = Color(0xFFB0603A)
    )

    // protanopia
    val Protanopia = StarsPalette(
        name = "Protanopia",
        background = Color(0xFF0D1B2A),
        onBackground = Color(0xFFF5F5F5),
        surface = Color(0xFF1B263B),
        onSurface = Color(0xFFFFD166),
        accent1 = Color(0xFF005A8C),
        accent2 = Color(0xFFA35F00),
        accent3 = Color(0xFF3A6EA5),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFFFFD166),
        onHighlight = Color(0xFF0D1B2A),
        muted = Color(0xFF24344D),
        confirm = Color(0xFF0072B2),
        cancel = Color(0xFFA35F00)
    )

    // deuteranopia theme
    val Deuteranopia = StarsPalette(
        name = "Deuteranopia",
        background = Color(0xFFF3F6FB),
        onBackground = Color(0xFF102A43),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1F4E8C),
        accent1 = Color(0xFF1F4E8C),
        accent2 = Color(0xFFA35F00),
        accent3 = Color(0xFF3F7FBF),
        onAccent = Color(0xFFFFFFFF),
        highlight = Color(0xFF0072B2),
        onHighlight = Color(0xFFFFFFFF),
        muted = Color(0xFFD6E0EE),
        confirm = Color(0xFF1F4E8C),
        cancel = Color(0xFFA35F00)
    )

    val all = listOf(Legacy, Light, Dark, Nebula, Moonlight, Forest, Campfire, Dune, Protanopia, Deuteranopia)

    /**
     * Finds a preset by its saved name, if it doesnt exist then the default is Moonlight
     */
    fun byName(name: String): StarsPalette = all.find { it.name == name } ?: Moonlight
}