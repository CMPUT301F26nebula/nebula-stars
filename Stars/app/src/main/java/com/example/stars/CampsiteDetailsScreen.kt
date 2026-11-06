package com.example.stars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.apps.card.v1.Columns
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(navController: NavController,
                          currentUser: UserProfile,
                          campsite: Campsite,
                          campsiteRepository: CampsiteRepository,
                          imageRepo: ImageRepository,
                          userRepository: UserRepository,
                          lotteryRepository: LotteryRepository) {

    val ownerName = runBlocking { userRepository.getUser(campsite.ownerId)?.name }


    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // white box in middle which holds all buttons
        Box(
            modifier = Modifier
                .background(
                    Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 30.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .size(height = 500.dp, width = 310.dp)
        )
        {
            Column(modifier = Modifier.clip(shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 30.dp,
                bottomStart = 10.dp,
                bottomEnd = 10.dp
            ))) {

                Text(campsite.title, modifier = Modifier.fillMaxWidth().background(color = Color(0xFF008B8B)))

                ImageCarousel(
                    imageIds = campsite.imageIds,
                    imageRepo = imageRepo,
                    editor = false,
                    onDelete = {}
                )


                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {

                    // TODO: implement star system
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(Color(0x00000000), contentColor = Color.Black)){
                        Text("Rate: (stars)")
                    }

                    Text(text = "$${campsite.price}")
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween){
                    Text("Owner: $ownerName")

                    Text("Capacity: ${campsite.capacity}")
                }

                if (currentUser.permission == UserPermission.ADMIN){
                    AdminCampsiteActions(campsite, userRepository, campsiteRepository)
                }


                    Text(campsite.description, Modifier.verticalScroll(rememberScrollState()))



                if (currentUser.id == campsite.ownerId){
                    Button(onClick = {navController.navigate("edit campsite $campsite")}){
                    }
                }

            }
        }
    }
}
