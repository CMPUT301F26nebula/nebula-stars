package com.example.stars

/**
 * stuff to do with the owner viewing their own campsites from the owners tab
 */


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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OwnerViewCampsites(navController: NavController,
                       campsiteRepository: CampsiteRepository,
                       imageRepository: ImageRepository,
                       currentUser: UserProfile
){
    var search by remember { mutableStateOf("") }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList())}
    var myCampsites by remember { mutableStateOf<List<Campsite>>(emptyList())}
    LaunchedEffect(Unit) {
        campsites = campsiteRepository.getAll()

        val myCampsitesBuild: ArrayList<Campsite> = arrayListOf()
        for (campsite in campsites){
            if (campsite.ownerId == currentUser.id){
                myCampsitesBuild.add(campsite)
            }
        }
        myCampsites = myCampsitesBuild
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
        ,//.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "MY CAMPSITES"
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (myCampsites.isEmpty()) {
                Text("No campsites found", color = StarsColors.OnBackground)
            } else {
                // List of campsites
                LazyColumn {
                    itemsIndexed(campsites) { index, campsite ->

                        CampsiteRow(
                            campsite = campsite,
                            color = StarsColors.Accent1,
                            onClick = {navController.navigate("campsite/${Uri.encode(campsite.id)}")},
                            imageRepository = imageRepository
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                    }
                }
            }
        }
    }
}