package com.example.stars

/**
this entire page was written by claude with the input of the doc/muckups.html
and the promts: "is the background of user profile update and image?",
"can you create the image of the background so I can set my app background as that?",
and "please write the code for the background"
Claude sonnet 5.5, 2026-11-1
https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
 */
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun StarsBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,

) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height

                // Mockup proportions: a 450 x 800 frame split into 4 stripes
                val stripeW = w / 4f
                val stripeH = w * (800f / 450f)
                val top = h - stripeH

                // Base color
                drawRect(StarsColors.Purple)

                // Rotate around the bottom-right corner, like the mockup
                withTransform({ rotate(-45f, Offset(w, h)) }) {
                    // Widest stripe first, narrower ones drawn on top,
                    // so there are no hairline gaps between stripes
                    drawRect(StarsColors.Yellow, Offset(0f, top), Size(stripeW * 4, stripeH))
                    drawRect(StarsColors.Magenta, Offset(0f, top), Size(stripeW * 3, stripeH))
                    drawRect(StarsColors.Cyan, Offset(0f, top), Size(stripeW * 2, stripeH))
                    // The fourth (Indigo) stripe is the same as the base, so it's skipped
                }
            },
        content = content,

    )
}

@Preview
@Composable
fun BackgroundPreview() {
    StarsBackground() { }
}