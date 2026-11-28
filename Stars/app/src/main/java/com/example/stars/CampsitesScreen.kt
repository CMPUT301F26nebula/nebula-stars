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


/**
 * Screen to browse all the campsites
 *
 * @param filter the filter currently applied to the list
 * @param sort the sort currently applied to the list
 * @param onFilterChange called with the new filter and sort when the user applies them
 */
@Composable
fun CampsitesScreen(
    navController: NavController,
    campsiteRepository: CampsiteRepository,
    imageRepository: ImageRepository,
    filter: CampsiteFilter,
    sort: CampsiteSort,
    onFilterChange: (CampsiteFilter, CampsiteSort) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList())}
    var showFilter by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        campsites = campsiteRepository.getAllCampsites()
    }
    val results = applySort(applyFilter(searchCampsites(campsites, search), filter), sort)
    val filtersOn = filter.isActive || sort.field != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarsColors.Background)
            ,//.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "CAMPSITES"
        ) {
            // Filter button
            Button(
                onClick = { showFilter = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filtersOn) StarsColors.Accent3 else StarsColors.Surface,
                    contentColor = if (filtersOn) StarsColors.OnAccent else StarsColors.OnSurface
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
        Button(
            onClick = {
                navController.navigate("scan qr code")
            },
            colors = ButtonDefaults.buttonColors(
            containerColor = StarsColors.Surface,
            contentColor = StarsColors.OnSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("scanQrButton")
        ) {
            Text("Scan QR Code")
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
                Text("No campsites found", color = StarsColors.OnBackground)
            } else {
                // List of campsites
                LazyColumn {
                    itemsIndexed(results) { index, campsite ->
                        CampsiteRow(
                            campsite = campsite,
                            color = RandomStarsColor.fromId(campsite.id),
                            onClick = { navController.navigate("campsite/${Uri.encode(campsite.id)}") },
                            imageRepository = imageRepository
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
                .background(StarsColors.Surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search field
            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text(
                        text = "Search or #tags",
                        color = StarsColors.OnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                textStyle = TextStyle(
                    color = StarsColors.OnSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = StarsColors.OnSurface,
                    unfocusedIndicatorColor = StarsColors.OnSurface.copy(alpha = 0.6f),
                    cursorColor = StarsColors.OnSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("searchField")
            )
        }
    }
    // filter popup
    if (showFilter) {
        FilterDialog(
            currentFilter = filter,
            currentSort = sort,
            onApply = { newFilter, newSort ->
                onFilterChange(newFilter, newSort)
                showFilter = false
            },
            onDismiss = { showFilter = false }
        )
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

    if (s.startsWith("#")) {
        val searchTags = extractTags(s)
        if (searchTags.isEmpty()) return campsites // user has only typed "#" so far
        return campsites.filter { extractTags(it.description).containsAll(searchTags) }
    }

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
 * Pulls every #tag out of some text, lowercase and without the #
 * for example: "Nice spot #BEACH #Party" -> {"beach", "party"}
 */
fun extractTags(text: String): Set<String> {
    return Regex("#(\\w+)")
        .findAll(text)
        .map { it.groupValues[1].lowercase() }
        .toSet()
}