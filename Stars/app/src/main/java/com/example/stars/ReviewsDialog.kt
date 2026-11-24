/*
Contains the popup that lists every review of a campsite
 */

package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Popup showing a campsite's avg rating, the current user's own review and every other review
 *
 * @param average the campsite's average rating
 * @param count the number of reviews the campsite has
 * @param ratings every rating of the campsite, newest first
 * @param reviewerNames user ID -> display name, used to show who wrote each review
 * @param userRating the current user's rating of the campsite, or null if they havent
 * @param canReview true if the current user has stayed here , so they can write or edit a review
 * @param onWriteReview called when the user presses Write a review/Edit your review
 * @param onDismiss called when the user presses the close button *
 */

@Composable
fun ReviewsDialog (
    average: Double,
    count: Int,
    ratings: List<Rating>,
    reviewerNames: Map<String, String>,
    userRating: Rating?,
    canReview: Boolean,
    onWriteReview: () -> Unit,
    onDismiss: () -> Unit
) {
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
                .background(StarsColors.White)
        ) {
            //header
            Text(
                "Reviews",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = StarsColors.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarsColors.Purple)
                    .padding(10.dp)
            )

            //scrolling body
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //avg rating
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        StarRatingDisplay(
                            average = average,
                            count = if (count == 0) 0 else null,
                            color = StarsColors.Yellow,
                            starSize = 32.sp,
                            numberSize = 24.sp,
                            numberColor = StarsColors.Purple
                        )
                    }
                }
                //current user's review (if they stayed there)
                if (canReview) {
                    item {
                        Text(
                            "Your review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.Cyan

                        )
                    }
                    item {
                        if (userRating != null) {
                            ReviewItem(rating = userRating, reviewerName = "You")
                        } else {
                            Text("No review yet")
                        }
                    }
                    item {
                        Button(
                            onClick = onWriteReview,
                            colors = ButtonDefaults.buttonColors(StarsColors.Cyan)
                        ) {
                            Text(
                                if (userRating == null) "Write a review" else "Edit your review",
                                color = StarsColors.White
                            )
                        }
                    }
                }
                //every review
                item {
                    Text(
                        "All reviews ($count)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.Purple
                    )
                }
                if (ratings.isEmpty()) {
                    item {
                        Text("No reviews yet")
                    }
                } else {
                    items(ratings) { rating ->
                        ReviewItem(
                            rating = rating,
                            reviewerName = reviewerNames[rating.userId] ?: "Unknown User"
                        )
                    }
                }
            }
            //close button
            Button(
                onClick = onDismiss,
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                modifier= Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    "Close",
                    color = StarsColors.White,)
            }
        }
    }
}


/**
 * One review which contains who wrote it, their stars/rating, when they stayed, and their comment if they wrote one
 *
 * @param rating the review to show
 * @param reviewerName the name of the person who wrote the review
 */

@Composable
private fun ReviewItem(rating: Rating, reviewerName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                reviewerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            StarRatingDisplay(
                average = rating.stars,
                count = null,
                color = StarsColors.Yellow,
                numberColor = StarsColors.Purple
            )
        }

        if (rating.stayRange.end != 0L) {
            Text(
                "Stayed ${formatStayRange(rating.stayRange)}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (rating.comment.isNotBlank()) {
            Text(
                rating.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


//helper func for making a stay range with epoch range
private fun formatStayRange(range: EpochRange): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.CANADA)
    return "${format.format(Date(range.start))} - ${format.format(Date(range.end))}"
}