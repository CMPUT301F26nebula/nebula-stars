package com.example.stars

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A screen that only admins have access to from (currently) the home screen.
 * Shows a list of all users
 */
@Composable
fun AdminViewUserProfiles(navController: NavController, userRepository: UserRepository, imageRepository: ImageRepository, currentUser: UserProfile){

    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    /*
    different code simplified by claude prompt: "how do I fix out of order updates"
    claude sonnet 5.5 2026-11-23
    https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
     */
    LaunchedEffect(Unit) {
        userRepository.addListener { updated ->
            users = updated.filterNotNull()
        }
    }

    // column to hold things and make background purple
    Column(modifier = Modifier.fillMaxSize().background(color = StarsColors.Purple)) {

        ScreenHeader(
            navController = navController,
            title = "Users"
        )

        // column for each individual user card
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

            itemsIndexed(users) { index, user ->

                // so that you can't ban or delete yourself from here
                if (user != currentUser) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminUserRow(navController, user, userRepository, imageRepository)
                }
            }
        }
    }
}

/**
 * this screen shows all the users that exist, their name, email, phone, and permission level,
 * and offers ban owner and delete user options
 */
@Composable
fun AdminUserRow(navController: NavController, userProfile: UserProfile, userRepository: UserRepository, imageRepository: ImageRepository){



    var image by remember {mutableStateOf<String?>(null)}
    LaunchedEffect(Unit) {

        if (userProfile.profilePic != "") {
            image = imageRepository.getImageUri(
                userProfile.profilePic
            )
        }
        Log.d("Admin User Row","Image ID: "+ image)
    }


    // show actual buttons if true
    var confirmDeletion by remember { mutableStateOf(false) }
    var confirmBan by remember { mutableStateOf(false) }

    // used so that the card isn't shown after deletion since userRepository isn't updating
    // until screen change (I think)
    var deleted by remember { mutableStateOf(false) }


    // default permission level string since it is not a string in the class
    var permissionLevel by remember { mutableStateOf("Entrant") }
    if (userProfile.permission == UserPermission.ADMIN){
        permissionLevel = "Admin"
    }
    if (userProfile.permission == UserPermission.OWNER){
        permissionLevel = "Owner"
    }

    // if the user has not been deleted
    if (!deleted) {

        Card(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 16.dp)
                .clickable(onClick = { navController.navigate("user profile/${Uri.encode(userProfile.id)}") }),
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

                    // permission level
                    Text(
                        text = permissionLevel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
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

                AsyncImage(
                    model = image,
                    contentDescription = "profile picture",
                    modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                    error = painterResource(R.drawable.default_pfp)
                )
            }

            // buttons to delete and ban
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {

                // if not trying to delete user, show the ban button
                if (!confirmBan) {
                    AdminActionOnUser(
                        confirmDeletion, "Delete User",
                        onRequest = {
                            confirmDeletion = true
                            confirmBan = false
                        },
                        onConfirm = {
                            runBlocking {
                                userRepository.deleteUser(userProfile)
                                confirmDeletion = false
                                deleted = true
                            }
                        },
                        onCancel = { confirmDeletion = false },
                        Modifier.weight(1f)
                    )
                }

                // if neither button is expanded put a space between them
                if (!confirmBan && !confirmDeletion) {
                    Spacer(modifier = Modifier.size(10.dp))
                }

                // if not trying to ban user show the delete button
                if (!confirmDeletion && userProfile.permission >= UserPermission.OWNER) {
                    AdminActionOnUser(
                        confirmBan, "Ban Owner",
                        onRequest = {
                            confirmDeletion = false
                            confirmBan = true
                        },
                        onConfirm = {
                            runBlocking { userRepository.banOwner(userProfile) }
                            confirmDeletion = false
                            deleted = true
                        },
                        onCancel = { confirmBan = false },
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * button to delete users. expands to confirm and cancel buttons
 * only admins should see this
 */
@Composable
fun AdminActionOnUser(confirm: Boolean, action: String, onRequest: () -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit, modifier: Modifier){


    // show the delete button if not AlreadyPressed
    if (!confirm) {
        Button(onRequest,

            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 5.dp,
                bottomStart = 5.dp,
                bottomEnd = 5.dp
            ),
            colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
            modifier = modifier) {
            Text(action, textAlign = TextAlign.Center, maxLines = 1, autoSize = TextAutoSize.StepBased(
                minFontSize = 1.sp,
                maxFontSize = 30.sp,
                stepSize = 1.sp
            ),
                color = StarsColors.White)
        }
    }

    // show the confirm and cancel buttons
    if (confirm) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally
            ), verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {

            Button(onConfirm,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                modifier = Modifier.testTag("Confirm").weight(1f)) {
                Text("Confirm",textAlign = TextAlign.Center, maxLines = 1, autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 50.sp,
                    stepSize = 1.sp
                ),
                    color = StarsColors.White)
            }

            Spacer(modifier = Modifier.size(10.dp))

            Button(onCancel,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                modifier = Modifier.testTag("Cancel").weight(1f)) {
                Text("Cancel", textAlign = TextAlign.Center, maxLines = 1, autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 50.sp,
                    stepSize = 1.sp
                ),
                    color = StarsColors.White)
            }

        }

    }

}


/**
 * shows on campsite card. Allows admins to ban owners and remove campsite listings
 * banning owners demotes them to ENTRANTS
 */
@Composable
fun AdminCampsiteActions(campsite: Campsite, userRepository: UserRepository, campsiteRepository: CampsiteRepository){
    var showButtons by remember { mutableStateOf(false) }

    if (!showButtons){
        Column() {

            Button(
                onClick = { showButtons = true },
                modifier = Modifier.testTag("Admin Actions").size(height = 40.dp, width = 310.dp),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Yellow)
            ) {
                Text("Show Admin Actions",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarsColors.White)
            }

            Spacer(modifier = Modifier.size(height = 40.dp, width = 310.dp))
        }
    }
    if (showButtons){

        Column() {

            Button(onClick = {showButtons = false},
                modifier = Modifier.testTag("Admin Actions").size(height = 40.dp, width = 310.dp),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(StarsColors.Yellow)){
                Text("Hide Admin Actions",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarsColors.White)
            }

            Row() {

                Button(
                    onClick = {
                        val user = runBlocking { userRepository.getWithId(campsite.id) }

                        if (user != null) {
                            runBlocking { userRepository.banOwner(user) }
                        }
                    },
                    modifier = Modifier.testTag("Ban Owner").size(height = 40.dp, width = 155.dp),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(StarsColors.Magenta)
                ) {
                    Text("Ban Owner",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarsColors.White)
                }

                Button(
                    onClick = {
                        runBlocking { campsiteRepository.removeCampsite(campsite.id) }
                    },
                    modifier = Modifier.testTag("Remove Campsite")
                        .size(height = 40.dp, width = 155.dp),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 10.dp
                    ),
                    colors = ButtonDefaults.buttonColors(StarsColors.Cyan)

                ) {
                    Text("Remove Listing",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarsColors.White)
                }

            }
        }
    }
}