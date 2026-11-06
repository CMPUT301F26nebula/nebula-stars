package com.example.stars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.apps.card.v1.Columns
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.Permissions

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
    var corner by remember { mutableStateOf(10.dp) }

    if (currentUser.permission == UserPermission.ADMIN){
        corner = 0.dp

    }

    Button(onClick = { navController.popBackStack() },
        modifier = Modifier.systemBarsPadding(),
        colors = ButtonDefaults.buttonColors(starsCyan),
        shape = RoundedCornerShape(
            topStart = 5.dp,
            topEnd = 5.dp,
            bottomStart = 5.dp,
            bottomEnd = 5.dp
        ),) {
        Text("<-")
    }


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
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
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

                Row(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF008B8B))
                    .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        campsite.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )

                }

                ImageCarousel(
                    imageIds = campsite.imageIds,
                    imageRepo = imageRepo,
                    editor = false,
                    onDelete = {}
                )


                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {

                    Text("Owner: $ownerName",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    // TODO: implement star system
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(Color(0x00000000), contentColor = Color.Black),
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ){
                        Text("Rate: (stars)", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium)
                    }

                }

                Row(modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween){


                    Text("Capacity: ${campsite.capacity}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)

                    Text(text = "$${campsite.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center)
                }

                LazyColumn(modifier = Modifier.padding(horizontal = 10.dp, vertical = 0.dp)) {
                    item{Text("Description:",  style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                        )
                    }

                    item {Text(campsite.description)}

                }



            }
        }




        if (currentUser.id == campsite.ownerId){
            Row() {

                Button(
                    onClick = { navController.navigate("edit campsite ${Uri.encode(campsite.id)}") },
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = corner,
                        bottomEnd = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(starsMagenta),
                    modifier = Modifier.size(height = 40.dp, width = 155.dp),
                ) {
                    Text("Edit Campsite",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                //TODO: implement this button
                Button(onClick = {},
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = corner
                    ),
                    colors = ButtonDefaults.buttonColors(starsCyan),
                    modifier = Modifier.size(height = 40.dp, width = 155.dp),
                    ) {
                    Text("View Entrants",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        else{

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = corner
                    ),
                    colors = ButtonDefaults.buttonColors(starsCyan),
                    modifier = Modifier.size(height = 40.dp, width = 310.dp),
                ) {
                    Text(
                        "Lotteries",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

        }


        // off set of y= -1 since there was a single pixel gap for some reason
        Box(Modifier.offset(y= (-1).dp)) {
            if (currentUser.permission == UserPermission.ADMIN) {
                AdminCampsiteActions(campsite, userRepository, campsiteRepository)
            }
        }
    }

}
