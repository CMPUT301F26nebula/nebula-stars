package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun CampsitesScreen(navController: NavController) {
    var search by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
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
                Text("V")
            }
        }

        // Content
        Column(
            //modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn() {
                /*
                items(campsites) { campsite ->
                    CampsiteRow(campsite)

                    Spacer(modifier = Modifier.height(12.dp))
                }
                 */
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

            // Search button
            Button(
                onClick = {

                },
                modifier = Modifier.testTag("searchButton") // for testing
            ) {
                Text("Go")
            }
        }
    }
}

@Preview
@Composable
fun CampsitesScreenPreview() {
    CampsitesScreen(rememberNavController())
}