/*
Contains composables for showing and entering star ratings
 */
package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
 * @param starSize font size of the filled stars (default 16.sp)
 * @param numberSize font size of the number (default 16.sp)
 */

@Composable
fun StarRatingDisplay(
    average: Double,
    count: Int?,
    modifier: Modifier = Modifier,
    color: Color = StarsColors.OnSurface,
    starSize: TextUnit = 16.sp,
    numberSize: TextUnit = 16.sp,
    numberColor: Color = StarsColors.OnSurface
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
                style = MaterialTheme.typography.bodyLarge,
                fontSize = starSize,
                modifier = Modifier.alignByBaseline()
            )
            // avg rating (4.3)
            Spacer(Modifier.width(6.dp))
            Text(
                averageText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = numberSize,
                color = numberColor,
                modifier = Modifier.alignByBaseline()
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

    val cardShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 30.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(StarsColors.OnBackground)
        ) {
            //header
            Text(
                "Rate Campsite",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = StarsColors.OnBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarsColors.Background)
                    .padding(10.dp)
            )

            //body
            Column(modifier = Modifier.padding(16.dp)) {
                StarRatingDisplay(
                    average = stars,
                    count = null,
                    color = StarsColors.Accent3,
                    starSize = 48.sp,
                    numberSize = 40.sp,
                    numberColor = StarsColors.OnSurface
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..5f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = StarsColors.OnSurface,
                        activeTrackColor = StarsColors.OnSurface,
                        inactiveTrackColor = StarsColors.OnSurface.copy(alpha = 0.2f),
                        activeTickColor = StarsColors.Surface,
                        inactiveTickColor = StarsColors.OnSurface
                    )
                )

                // comment box
                OutlinedTextField(
                    value = comment,
                    onValueChange = { newText ->
                        if (newText.length <= MAX_COMMENT_LENGTH) comment = newText
                    },
                    label = { Text("How was your experience? (Optional)") },
                    supportingText = { Text("${comment.length}/$MAX_COMMENT_LENGTH") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StarsColors.OnSurface,
                        focusedLabelColor = StarsColors.OnSurface,
                        cursorColor = StarsColors.OnSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(StarsColors.Confirm),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Cancel", color = StarsColors.OnAccent)
                }
                Button(
                    onClick = { onSubmit(stars, comment) },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(StarsColors.Cancel),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Submit", color = StarsColors.OnAccent)
                }
            }
        }
    }
}