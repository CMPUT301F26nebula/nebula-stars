package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

val maxHeight: Int = 4
val boxSize: Int = 180

data class HomeButtonData(val text: String, val tag: String, val navRoute: String)

//replaces the buttons
@Composable
fun HomeLayoutBox(navController: NavController, data: HomeButtonData, height: Dp, width: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            //These have a specific order
            .height(height)
            .width(width)
            .clip(RoundedCornerShape(30.dp))
            .background(color)
            .padding(10.dp)
            .clickable(onClick = {
                navController.navigate(data.navRoute)
            })
            .testTag(data.tag),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = data.text,
            color = starsWhite,
            fontSize = (boxSize/7).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

//TODO fix text fitting (currently slides under next box to the right)
@Composable
fun HomeLayout(navController: NavController, boxDatas: Array<HomeButtonData>, modifier: Modifier = Modifier) {
    var i = boxDatas.size-1
    var row: Int
    var column: Int
    Box(contentAlignment = Alignment.BottomEnd, modifier = modifier) {
        for (data in boxDatas) {
            row = (i%maxHeight)
            column = (i/maxHeight)
            HomeLayoutBox(navController, data, ((row+1)*boxSize - column*50).dp, (((column+1)*boxSize) - row*10).dp,
                when ((i + column)%3) {
                    0 -> starsYellow
                    1 -> starsCyan
                    2 -> starsMagenta
                    else -> starsCyan
                })
            i--
        }
    }
}

/*
@Preview
@Composable
fun BoxPreview() {
    HomeLayoutBox(rememberNavController(), HomeButtonData("test", "testButton", "testScreen"), 100.dp, 100.dp, starsYellow)
}

 */

@Preview
@Composable
fun LayoutPreview() {
    HomeLayout(rememberNavController(), arrayOf(
        HomeButtonData("test1", "testButton", "testScreen"),
        HomeButtonData("test2", "testButton", "testScreen"),
        HomeButtonData("test3", "testButton", "testScreen"),
        HomeButtonData("test4", "testButton", "testScreen"),
        HomeButtonData("test5", "testButton", "testScreen"),
        HomeButtonData("test6", "testButton", "testScreen"),
    ))
}