package com.example.stars

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
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
                          lotteryRepository: LotteryRepository,
                          reservationRepository: ReservationRepository) {

    // get the name of the owner
    val ownerName = runBlocking { userRepository.getWithId(campsite.ownerId)?.name }

    // get the existence of a lottery or it's object
    var lottery: Lottery? by remember { mutableStateOf(null) }
    LaunchedEffect(campsite.id) {

        val lotteries = lotteryRepository.getWithUserId(campsite.ownerId)

        for (individualLottery in lotteries) {
            if (campsite.id in individualLottery.campsiteIDs) {
                lottery = individualLottery
            }
        }

    }

    // campsite shown on this screen, reloaded after rating so the new average shows right away
    var shownCampsite by remember(campsite.id) { mutableStateOf(campsite) }
    // the current user's most recent finished stay here , or null if they havent stayed
    var completedStay: Reservation? by remember(campsite.id) { mutableStateOf(null) }
    // the current user's existing rating, or null if they havent rated this campsite
    var userRating: Rating? by remember(campsite.id) { mutableStateOf(null) }
    // true while the rating popup is open
    var showRatingDialog by remember { mutableStateOf(false) }
    // used to run the firestore save when the user presses Submit
    val scope = rememberCoroutineScope()
    // true while the review popup is opne
    var showReviewsDialog by remember { mutableStateOf(false) }
    // every rating of the campsite, newest first
    var ratings by remember(campsite.id) { mutableStateOf<List<Rating>>(emptyList()) }
    // user id -> display name of every user who has written a review
    var reviewerNames by remember(campsite.id) { mutableStateOf<Map<String, String>>(emptyMap()) }

    //loads every review and the names fo the users
    suspend fun loadReviews() {
        val loaded = campsiteRepository.getRatings(campsite.id)
        ratings = loaded
        reviewerNames = loaded.associate { rating ->
            rating.userId to (userRepository.getWithId(rating.userId)?.name ?: "Unknown User")
        }
    }

    // load thee reviews each time the popup opens
    LaunchedEffect(showReviewsDialog) {
        if (showReviewsDialog) loadReviews()
    }


    //check if the user can rate, and load their old rating if they had one
    LaunchedEffect(campsite.id, currentUser.id) {
        val stay = reservationRepository.getLatestCompletedStay(currentUser.id, campsite.id)
        completedStay = stay
        if (stay != null) {
            userRating = campsiteRepository.getUserRating(campsite.id, currentUser.id)
        }
    }

    val currentLottery = lottery

    // this is so that the buttons can stack and still have rounded corners on the last button
    var corner by remember { mutableStateOf(10.dp) }
    if (currentUser.permission == UserPermission.ADMIN){
        corner = 0.dp

    }


    ScreenHeader(
        navController = navController,
        title = "CAMPSITE"
    )

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // white box in middle which holds all buttons
        Box(
            modifier = Modifier
                .background(
                    StarsColors.White,
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
                bottomEnd = 10.dp,
            )).fillMaxWidth()) {

                Row(modifier = Modifier.fillMaxWidth().background(color = StarsColors.Cyan)
                    .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {

                    // title
                    Text(
                        campsite.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = StarsColors.White,
                    )

                }


                Box(Modifier.fillMaxWidth()) {
                    // if there is more than one image show the carousel
                    if (campsite.imageIds.size > 1) {
                        // images
                        ImageCarousel(
                            imageIds = campsite.imageIds,
                            imageRepo = imageRepo,
                            editor = false,
                        )
                    }

                    // if there is only one image show only that image
                    else if (campsite.imageIds.size == 1) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = runBlocking { imageRepo.getImageUri(campsite.imageIds[0]) },
                                contentDescription = "campsite Image",
                                modifier = Modifier,
                                contentScale = ContentScale.Fit
                            )
                        }

                    }
                }



                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {

                    //name of the owner
                    Text(
                        text = "Owner: $ownerName",
                        modifier = Modifier.clickable(onClick = { navController.navigate("user profile/${Uri.encode(campsite.ownerId)}") }),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    //reviews section (clickable and brings you to review page)
                    Row(
                        modifier = Modifier.clickable { showReviewsDialog = true }
                    ) {
                        Text(
                            "Rate: ",
                            fontWeight = FontWeight.Medium
                        )

                        StarRatingDisplay(
                            average = shownCampsite.averageRating,
                            count = null, //shownCampsite.ratingCount  <- replace null with this for review count if you want it
                            color = StarsColors.Yellow,
                            numberColor = StarsColors.Black
                        )
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

                if (showReviewsDialog) {
                    ReviewsDialog(
                        average = shownCampsite.averageRating,
                        count = shownCampsite.ratingCount,
                        ratings = ratings,
                        reviewerNames = reviewerNames,
                        userRating = userRating,
                        canReview = completedStay != null,
                        onWriteReview = { showRatingDialog = true },
                        onDismiss = { showReviewsDialog = false },
                        isAdmin = currentUser.permission == UserPermission.ADMIN,
                        onRemoveRating = { rating ->
                            scope.launch {
                                campsiteRepository.deleteRating(campsite.id, rating.userId)
                                loadReviews()
                                campsiteRepository.getWithId(campsite.id)?.let { shownCampsite = it }
                                userRating = campsiteRepository.getUserRating(campsite.id, currentUser.id)
                            }
                        },
                        onRemoveComment = { rating ->
                            scope.launch {
                                campsiteRepository.removeComment(campsite.id, rating.userId)
                                loadReviews()
                                userRating = campsiteRepository.getUserRating(campsite.id, currentUser.id)
                            }
                        }
                    )
                }

                if (showRatingDialog) {
                    RatingDialog(
                        // start with the user's old rating if they have one
                        initialStars = userRating?.stars ?: 1.0,
                        initialComment = userRating?.comment ?: "",
                        onDismiss = { showRatingDialog = false },
                        onSubmit = { stars, comment ->
                            showRatingDialog = false
                        scope.launch {
                            campsiteRepository.setRating(
                                campsite.id,
                                currentUser.id,
                                stars,
                                comment,
                                completedStay?.timeRange ?: EpochRange()
                            )
                            // reload so the new avg stars and the users rating shows straight away
                            userRating = campsiteRepository.getUserRating(campsite.id, currentUser.id)
                            campsiteRepository.getWithId(campsite.id)?.let { shownCampsite = it }
                            loadReviews()
                            }
                        }
                    )
                }
            }
        }

        // show the owner buttons if owner
        if (currentUser.id == campsite.ownerId){

            // had a pixel of background between button and box, so I fixed it with this offset
            Row(Modifier.offset(y= (-1).dp)) {


                // if there is a lottery show view entrants
                if (currentLottery != null) {

                    //
                    Button(
                        onClick = { navController.navigate("entrants/${currentLottery.id}") },
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
                            "View Entrants",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarsColors.White
                        )
                    }
                }

                // if there isn't a lottery say so
                if (currentLottery == null) {
                    Button(
                        onClick = {navController.navigate("upload lottery")},
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = corner,
                            bottomEnd = 0.dp
                        ),
                        colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                        modifier = Modifier.size(height = 40.dp, width = 155.dp),
                    )

                    {
                        Text(
                            "Create Lottery",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarsColors.White
                        )
                    }
                }

                Button(
                    onClick = { navController.navigate("edit campsite ${Uri.encode(campsite.id)}") },
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = corner
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
                                    lotteryRepository.addOrUpdate(currentLottery)
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
                                    lotteryRepository.addOrUpdate(currentLottery)
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
        val lottery = runBlocking {lotteryRepository.getWithId(lotteryID)}

        // if there is a lottery (there should always be one but kotlin wanted to make sure)
        if (lottery != null) {

            // column for each individual user card
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

                // for each entrant make a card
                itemsIndexed(lottery.entrantIDs) { index, entrant ->
                    val profile = runBlocking { userRepository.getWithId(entrant) }

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = cardColors(StarsColors.White)
    ) {

        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            Column() {

                // Name
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,

                    )

                // email
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

            AsyncImage(
                model = userProfile.profilePic,
                contentDescription = "profile picture",
                modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                error = painterResource(R.drawable.default_pfp)
            )
        }

    }
}


