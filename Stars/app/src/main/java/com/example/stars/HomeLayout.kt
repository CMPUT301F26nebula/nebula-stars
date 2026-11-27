package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Data for one home screen tile
 */
data class HomeButtonData(val text: String, val tag: String, val navRoute: String, val icon: ImageVector)

private val tileShape = RoundedCornerShape(topStart = 10.dp, topEnd = 30.dp, bottomStart = 10.dp, bottomEnd = 10.dp)

// tile layout
@Composable
fun HomeTile(navController: NavController, data: HomeButtonData, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(tileShape)
            .background(color)
            .clickable { navController.navigate(data.navRoute) }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = null,
            tint = StarsColors.OnAccent,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = data.text,
            color = StarsColors.OnAccent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// tile colors
@Composable
fun HomeLayout(navController: NavController, rows: List<List<HomeButtonData>>, modifier: Modifier = Modifier) {
    val accents = listOf(StarsColors.Accent1, StarsColors.Accent2, StarsColors.Accent3)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for ((rowIndex, row) in rows.withIndex()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for ((colIndex, data) in row.withIndex()) {
                    HomeTile(
                        navController = navController,
                        data = data,
                        // diagonal pattern so no two touching tiles share a color
                        color = accents[(rowIndex + colIndex) % accents.size],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
