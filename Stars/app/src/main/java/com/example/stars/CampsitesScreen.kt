package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Screen to browse all the campsites
 */
@Composable
fun CampsitesScreen(
    navController: NavController,
    campsiteRepository: CampsiteRepository
) {
    var search by remember { mutableStateOf("") }
    val results = searchCampsites(campsiteRepository.campsites, search)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.testTag("backButton") // for testing
            ) {
                Text("<-")
            }

            // Title
            Text(
                text = "Campsites",
                fontSize = 32.sp
            )

            // Filter button
            Button(
                onClick = {

                },
                modifier = Modifier.testTag("filterButton") // for testing
            ) {
                Text("☰")
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (results.isEmpty()) {
                Text("No campsites found")
            } else {
                // List of campsites
                LazyColumn {
                    items(results) { campsite ->
                        CampsiteRow(campsite)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }


        // Search bar
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search field
            TextField(
                value = search,
                onValueChange = {
                    search = it // update location to new value
                },
                label = {
                    Text("Search")
                },
                modifier = Modifier
                    .testTag("searchField") // for testing
                    .weight(1f)
            )

            // Search button -- Commented because of live-update searching
//            Button(
//                onClick = {
//
//                },
//                modifier = Modifier.testTag("searchButton") // for testing
//            ) {
//                Text("Go")
//            }
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

/**
@Preview
@Composable
fun CampsitesScreenPreview() {
    CampsitesScreen(rememberNavController())
}
*/