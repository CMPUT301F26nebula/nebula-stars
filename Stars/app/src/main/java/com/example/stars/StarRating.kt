/*
Contains composables for showing and entering star ratings
 */
package com.example.stars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

/**
 * shows a rating out of 5 stars, followed by the number and the rating count.
 * for example: ★★★★☆ 4.3 (52)
 *
 * @param average the rating to show, from 0.0 to 5.0. A star fills once the average reaches .7,
 * so 0.7-1.6 is one star, 1.7-2.6 is two stars, 4.7-5.0 is five stars
 * @param count the number of ratings for this campsite
 * @param modifier modifier for the row
 * @param color color of the filled stars (white)
 */

@Composable
fun StarRatingDisplay(
    average: Double,
    count: Int?,
    modifier: Modifier = Modifier,
    color: Color = StarsColors.White
) {
    // the avg as text with one decimal place (4.6667 -> 4.7)
    val averageText = String.format(Locale.CANADA, "%.1f", average)

    // the avg in tenths (4.6667 -> 47)
    val tenths = (average * 10).roundToInt()

    // number of full stars: .7 and above rounds up
    // e.g. 0.0-0.6 -> 0, 0.7-1.6 -> 1, 1.7-2.6 -> 2, ... 4.7-5.0 -> 5
    val filledStars = (tenths + 3) / 10
    // filledStars = 4 -> ★★★★☆
    val starsText = "★".repeat(filledStars) + "☆".repeat(5 - filledStars)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(starsText, color = color, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(6.dp))

        if (count == 0) {
            Text("No ratings yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(averageText, style = MaterialTheme.typography.bodyLarge)
            if (count != null) {
                Text(" ($count)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
