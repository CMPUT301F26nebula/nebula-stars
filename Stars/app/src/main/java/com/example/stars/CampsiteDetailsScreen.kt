package com.example.stars

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking

/**
 * the expanded view of a campsites details. if you are a user you can join/leave an active lottery
 * from here. If you are the owner you can edit your campsite and see the people in the waiting list.
 * if you are an admin you can delete the listing or ban the owner.
 * TODO: add time and place details
 * BUG: images have white space on the left and do not fill
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(navController: NavController,
                          currentUser: UserProfile,
                          campsite: Campsite,
                          campsiteRepository: CampsiteRepository,
                          imageRepo: ImageRepository,
                          userRepository: UserRepository,
                          lotteryRepository: LotteryRepository) {

    // get the name of the owner
    val ownerName = runBlocking { userRepository.getUser(campsite.ownerId)?.name }

    // get the existence of a lottery or it's object
    var lottery: Lottery? by remember { mutableStateOf(null) }
    LaunchedEffect(campsite.id) {

        val lotteries = lotteryRepository.getUserLotteries(campsite.ownerId)

        for (individualLottery in lotteries) {
            if (campsite.id in individualLottery.campsiteIDs) {
                lottery = individualLottery
            }
        }
    }
    val currentLottery = lottery

    // this is so that the buttons can stack and still have rounded corners on the last button
    var corner by remember { mutableStateOf(10.dp) }
    if (currentUser.permission == UserPermission.ADMIN){
        corner = 0.dp

    }

    Box(Modifier.statusBarsPadding()) {
        BackButton(navController)
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

                    // title
                    Text(
                        campsite.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )

                }


                    // images
                    ImageCarousel(
                        imageIds = campsite.imageIds,
                        imageRepo = imageRepo,
                        editor = false,
                        onDelete = {}
                    )



                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {

                    //name of the owner
                    Text("Owner: $ownerName",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    // TODO: implement star system
                    Button(onClick = {},
                        colors = ButtonDefaults.buttonColors(Color(0x00000000), contentColor = Color.Black),
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

                Text("Description:",  style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                LazyColumn(modifier = Modifier.padding(horizontal = 10.dp, vertical = 0.dp)) {

                    item {Text(campsite.description)}

                }



            }
        }

        // show the owner buttons if owner
        if (currentUser.id == campsite.ownerId){

            // had a pixel of background between button and box, so I fixed it with this offset
            Row(Modifier.offset(y= (-1).dp)) {

                Button(
                    onClick = { navController.navigate("edit campsite ${Uri.encode(campsite.id)}") },
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = corner,
                        bottomEnd = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                    modifier = Modifier.size(height = 40.dp, width = 155.dp),
                ) {
                    Text("Edit Campsite",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarsColors.White
                    )
                }


                // if there is a lottery show view entrants
                if (currentLottery != null) {

                    //
                    Button(
                        onClick = { navController.navigate("entrants/${currentLottery.id}") },
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = corner
                        ),
                        colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                        modifier = Modifier.size(height = 40.dp, width = 155.dp),
                    ) {
                        Text(
                            "View Entrants",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarsColors.White
                        )
                    }
                }

                // if there isn't a lottery say so
                if (currentLottery == null) {
                    Box(modifier = Modifier
                        .background(
                            StarsColors.Cyan,
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = corner,
                                bottomEnd = 0.dp
                            )
                        )
                        .size(height = 40.dp, width = 155.dp),
                        contentAlignment = Alignment.Center)
                    {
                        Text(
                            "No Lottery",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarsColors.White
                        )
                    }
                }
            }
        }

        // if not the owner show the entrant buttons
        else{

            // offset because I had a one pixel gap
            Row(Modifier.offset(y= (-1).dp)) {

                // if there is a lottery show join/leave buttons
                if (currentLottery != null) {

                    // if you are not in the lottery show the join button.
                    if (currentUser.id !in currentLottery.entrantIDs){
                        Button(
                            onClick = {

                                // make the new list of entrants with you in it and update firebase
                                val enterList = currentLottery.entrantIDs.toMutableList()

                                // if you already joined it doesn't do anything
                                if (currentUser.id !in enterList) {
                                    enterList.add(currentUser.id)
                                    currentLottery.entrantIDs = enterList
                                    lotteryRepository.addOrUpdateLottery(currentLottery)
                                }
                            },
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = corner,
                                bottomEnd = 0.dp
                            ),
                            colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                            modifier = Modifier.size(height = 40.dp, width = 155.dp),
                        ) {
                            Text(
                                "Join Lottery",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarsColors.White
                            )
                        }

                        // are in the lottery show the exit button
                    } else {
                        Button(
                            onClick = {

                                // make the new list of entrants with you not in it and update firebase
                                val enterList = currentLottery.entrantIDs.toMutableList()

                                // if you already left don't try again
                                if (currentUser.id in enterList) {
                                    enterList.remove(currentUser.id)
                                    currentLottery.entrantIDs = enterList
                                    lotteryRepository.addOrUpdateLottery(currentLottery)
                                }


                            },
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = corner,
                                bottomEnd = 0.dp
                            ),
                            colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                            modifier = Modifier.size(height = 40.dp, width = 155.dp),
                        ) {
                            Text(
                                "Exit Lottery",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarsColors.White
                            )
                        }
                    }
                }

                // if there isn't a lottery, say so.
                if (currentLottery == null) {
                    Box(modifier = Modifier
                        .background(
                            StarsColors.Cyan,
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = corner,
                                bottomEnd = 0.dp
                            )
                        )
                        .size(height = 40.dp, width = 155.dp),
                        contentAlignment = Alignment.Center,)
                    {
                        Text(
                            "No Lottery",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarsColors.White,

                        )
                    }
                }

                // get the qr code for the campsite
                Button(onClick = {navController.navigate("qr code/${campsite.id}")},
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = corner
                    ),colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                    modifier = Modifier.size(height = 40.dp, width = 155.dp),

                ) {Text("QR Code",textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium)}
            }

        }


        // off set of y= -1 since there was a single pixel gap for some reason
        Box(Modifier.offset(y= (-2).dp)) {
            if (currentUser.permission == UserPermission.ADMIN) {
                AdminCampsiteActions(campsite, userRepository, campsiteRepository)
            }
        }
    }

}

/**
 * the list screen for the users in the lottery for the owner to see
 */
@Composable
fun OwnerViewUserProfiles(navController: NavController, lotteryID: String, userRepository: UserRepository, lotteryRepository: LotteryRepository){


    // column to hold things and make background purple
    Column(modifier = Modifier.fillMaxSize().background(color = StarsColors.Purple).statusBarsPadding()) {

        BackButton(navController)

        // get the lottery
        val lottery = runBlocking {lotteryRepository.getLottery(lotteryID)}

        // if there is a lottery (there should always be one but kotlin wanted to make sure)
        if (lottery != null) {

            // column for each individual user card
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

                // for each entrant make a card
                itemsIndexed(lottery.entrantIDs) { index, entrant ->
                    val profile = runBlocking { userRepository.getUser(entrant) }

                    if (profile != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OwnerUserRow(profile)
                    }
                }
            }
        }
    }
}

/**
 * this screen shows all the users that exist in the lottery,
 * their name, email, phone
 */
@Composable
fun OwnerUserRow(userProfile: UserProfile){


    // default permission level string since it is not a string in the class
    var permissionLevel by remember { mutableStateOf("Entrant") }
    if (userProfile.permission == UserPermission.ADMIN){
        permissionLevel = "Admin"
    }
    if (userProfile.permission == UserPermission.OWNER){
        permissionLevel = "Owner"
    }


    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Name
            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,

                )

            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            // used for phone number formating
            val nanpVisualTransformation = NanpVisualTransformation()

            // phone number
            Text(
                text = nanpVisualTransformation.filter(AnnotatedString(userProfile.phone)).text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}


