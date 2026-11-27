package com.example.stars

/**
 * the file for all things admins looking at pictures
 */

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.runBlocking


/**
 * the list screen of the pictures does not show pictures of unowned campsites
 * which isn't a problem since those should not exist.
 * shows the Owner of the campsites above their campsites if the owner has a pfp.
 * clicking on the name will bring you to the page associated with the user or campsite.
 */
@Composable
fun AdminViewPictures(navController: NavController, userRepository: UserRepository, campsiteRepository: CampsiteRepository, imageRepository: ImageRepository){

    // lists users and campsites to show
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    // watch for changes in fire base
    LaunchedEffect(Unit) {

        campsiteRepository.addListener { updated ->
            campsites = updated.filterNotNull()
        }

        userRepository.addListener { updated ->
            users = updated.filterNotNull()
        }

    }


    // the whole screen
    Column() {

        ScreenHeader(navController = navController, title = "IMAGES")

        Spacer(modifier = Modifier.padding(12.dp))

        // the column of cards that you see
        LazyColumn() {

            // for each user, get their profile pic and show that, then show their campsites
            users.forEach { user ->

                // if they don't have a profile pic show nothing
                if (user.profilePic != "") {
                    item() {
                        AdminViewProfilePicturesRow(user, imageRepository, userRepository)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // if they are an owner show their campsites after their profile pic if they have one
                if (user.permission >= UserPermission.OWNER) {
                    itemsIndexed(campsites) { index, campsite ->

                        // go over all campsites and pick only theirs
                        if (user.id == campsite.ownerId && campsite.imageIds.isNotEmpty()) {
                            AdminViewCampsitePicturesRow(
                                navController,
                                campsite,
                                imageRepository,
                                campsiteRepository
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

        }
    }
}

/**
 * this shows the card for the campsite pictures. The delete button works for the current center picture
 * and if you only have one then it fills the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViewCampsitePicturesRow(navController: NavController, campsite: Campsite, imageRepository: ImageRepository, campsiteRepository: CampsiteRepository){

    Card(
        colors  = CardDefaults.cardColors(
            contentColor = StarsColors.White,
            containerColor = StarsColors.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        // offset by -3 since that covers the top of the card
        Column(modifier = Modifier.fillMaxWidth().offset(y = -3.dp), horizontalAlignment = Alignment.CenterHorizontally) {


            // go to the campsite details page
            Button(onClick = {navController.navigate("campsite/${Uri.encode(campsite.id)}")},
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                modifier = Modifier.fillMaxWidth()
            ) {

                // campsite name
                Text(
                    text = campsite.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = StarsColors.OnAccent,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

            }


            // image or images with delete button
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                // if there is more than one image show the carousel
                if (campsite.imageIds.size > 1) {
                    // images
                    ImageCarousel(
                        imageIds = campsite.imageIds,
                        imageRepo = imageRepository,
                        editor = true,
                        onDelete = { image ->
                            imageRepository.deleteImage(image)
                            val newImages = campsite.imageIds.filter { it != image }
                            val newCampsite = campsite.copy(imageIds = newImages)
                            campsiteRepository.addOrUpdateWithId(campsite.id, newCampsite)

                        }
                    )
                }

                // if there is only one image show only that image
                else if (campsite.imageIds.size == 1){
                    Column(verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = runBlocking { imageRepository.getImageUri(campsite.imageIds[0]) },
                            contentDescription = "campsite Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Button(
                            onClick = {
                                imageRepository.deleteImage(campsite.imageIds[0])
                                val newImages = campsite.imageIds.filter { it != campsite.imageIds[0] }
                                val newCampsite = campsite.copy(imageIds = newImages)
                                campsiteRepository.addOrUpdateWithId(campsite.id, newCampsite)
                            },
                            content = { Text("Delete Image",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = StarsColors.OnAccent,) },
                            modifier = Modifier.height(50.dp).fillMaxWidth().offset(y = 3.dp).testTag("Delete image " + campsite.title),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
                            ),
                            colors = ButtonDefaults.buttonColors(StarsColors.Magenta)
                        )
                    }

                }

            }
        }
    }

}

/**
 * this shows the profile pictures of the users. delete deletes
 */
@Composable
fun AdminViewProfilePicturesRow(user: UserProfile, imageRepository: ImageRepository, userRepository: UserRepository){

    Card(
        colors  = CardDefaults.cardColors(
            contentColor = StarsColors.White,
            containerColor = StarsColors.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        // offset by -3 since that covers the top of the card
        Column(modifier = Modifier.fillMaxWidth().offset(y = -3.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // TODO: send click to users details
            Button(onClick = {},
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Yellow),
                modifier = Modifier.fillMaxWidth()
            ) {

                // users name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = StarsColors.OnAccent,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

            }


            // Image and delete button
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = runBlocking { imageRepository.getImageUri(user.profilePic) },
                            contentDescription = "profile picture",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )


                    Button(
                        onClick = {
                            imageRepository.deleteImage(user.profilePic)
                            val newCampsite = user.copy(profilePic = "")
                            userRepository.addOrUpdateWithId(user.id, newCampsite)
                        },
                        content = { Text("Delete Image",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = StarsColors.OnAccent,) },
                        modifier = Modifier.height(50.dp).fillMaxWidth().offset(y = 3.dp).testTag("Delete image " + user.name),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        ),
                        colors = ButtonDefaults.buttonColors(StarsColors.Magenta)
                    )
            }

        }

    }
}
