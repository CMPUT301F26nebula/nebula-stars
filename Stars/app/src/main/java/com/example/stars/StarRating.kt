/*
Contains composables for showing and entering star ratings
 */
package com.example.stars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

/**
 * shows a rating out of 5 stars, followed by the number and the rating count.
 * for example: ★★★★☆ 4.3 (52)
 *
 * @param average the rating to show, from 0.0 to 5.0. A star fills once the average reaches .7,
 * so 0.7-1.6 is one star, 1.7-2.6 is two stars, 4.7-5.0 is five stars
 * @param count the number of ratings for this campsite. if it has no ratings, no stars are displayed and it says "Unrated"
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
        if (count == 0) {
            Text(
                "Unrated",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        } else {
            // stars (★★★★☆)
            Text(
                starsText,
                color = color,
                style = MaterialTheme.typography.bodyLarge
            )
            // avg rating (4.3)
            Spacer(Modifier.width(6.dp))
            Text(
                averageText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            // rating count (23)
            if (count != null) {
                Text(
                    " ($count)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


// max character count for a rating comment
private const val MAX_COMMENT_LENGTH = 200

/**
 * Pop up where a user picks 1 to 5 stars and writes an optional comment
 *
 * @param initialStars where the slider starts (the beginning or the user's old star rating when editing)
 * @param initialComment comment to start with (empty or the user's old comment when editing)
 * @param onDismiss request the popup be dismissed
 * @param onSubmit called with the chosen stars and comment when the user clicks Submit
 */
@Composable
fun RatingDialog(
    initialStars: Double = 1.0,
    initialComment: String = "",
    onDismiss: () -> Unit,
    onSubmit: (stars: Double, comment: String) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(initialStars.toFloat()) }
    var comment by remember { mutableStateOf(initialComment) }

    // round to nearest factor of .5 (3.99998 -> 4.0, 3.5 -> 3.5, 3.0 -> 3.0)
    val stars = (sliderValue * 2).roundToInt() / 2.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this campsite") },
        text = {
            Column {
                StarRatingDisplay(
                    average = stars,
                    count = null,
                    color = StarsColors.Purple
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..5f,
                    steps = 7
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { newText ->
                        if (newText.length <= MAX_COMMENT_LENGTH) comment = newText
                    },
                    label = { Text("Describe your experience (optional)") },
                    supportingText = { Text("${comment.length}/$MAX_COMMENT_LENGTH") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(stars, comment) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
