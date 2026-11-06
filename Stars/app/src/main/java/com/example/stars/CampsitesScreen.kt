package com.example.stars

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// cyan, magenta, yellow, from top to bottom
val campsiteCardColors = listOf(
    StarsColors.Cyan,
    StarsColors.Magenta,
    StarsColors.Yellow
)

/**
 * Screen to browse all the campsites
 */
@Composable
fun CampsitesScreen(
    navController: NavController,
    campsiteRepository: CampsiteRepository
) {
    var search by remember { mutableStateOf("") }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList())}
    LaunchedEffect(Unit) {
        campsites = campsiteRepository.getFilteredCampsites()
    }
    val results = searchCampsites(campsites, search)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarsColors.Purple)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarsColors.White)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            BackButton(navController)

            // Title
            Text(
                text = "CAMPSITES",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StarsColors.Purple,
            )
            // Filter button
            Button(
                onClick = {
                    // for kaiwen to do : add filter
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StarsColors.White,
                    contentColor = StarsColors.Purple
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .testTag("filterButton") // for testing
            ) {
                Text(
                    text = "☰",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (results.isEmpty()) {
                Text("No campsites found", color = StarsColors.White)
            } else {
                // List of campsites
                LazyColumn {
                    itemsIndexed(results) { index, campsite ->
                        CampsiteRow(
                            campsite = campsite,
                            color = campsiteCardColors[index % campsiteCardColors.size],
                            onClick = {navController.navigate("campsite/${Uri.encode(campsite.id)}")}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }


        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(StarsColors.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search field
            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text(
                        text = "Search",
                        color = StarsColors.Purple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                textStyle = TextStyle(
                    color = StarsColors.Purple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = StarsColors.Purple,
                    unfocusedIndicatorColor = StarsColors.Purple.copy(alpha = 0.6f),
                    cursorColor = StarsColors.Purple
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("searchField")
            )
        }
    }
}

/**
 * Search function that returns a list of campsites that match the search
 * Title matches are displayed first, then description matches
 */
fun searchCampsites(campsites: List<Campsite>, search: String): List<Campsite> {
    val s = search.trim()
    // if no search, return all campsites (i.e. when the user first opens the campsite page)
    if (s.isEmpty()) return campsites

    // Splits search into matches and non matches
    val (titleMatches, others) = campsites.partition {
        it.title.contains(s, ignoreCase = true)
    }
    // Checks if the "non matches" match with descriptions
    val descriptionMatches = others.filter {
        it.description.contains(s, ignoreCase = true)
    }
    // Concats and returns list
    return titleMatches + descriptionMatches
}