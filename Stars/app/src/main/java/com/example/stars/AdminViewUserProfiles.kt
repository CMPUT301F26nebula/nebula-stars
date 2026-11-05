package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking

/**
 * A screen that only admins have access to from (currently) the home screen.
 * Shows a list of all users
 */
@Composable
fun AdminViewUserProfiles(navController: NavController, userRepository: UserRepository, campsiteRepository: CampsiteRepository){

    val users = runBlocking { userRepository.getUsersList() }

    // column to hold things and make background purple
    Column(modifier = Modifier.fillMaxSize().background(color = Color(0xFF4B0082))) {

        // back button
        Button(onClick = { navController.popBackStack() },
            modifier = Modifier.systemBarsPadding().testTag("Back"),
            colors = ButtonDefaults.buttonColors(Color(0xFF008B8B)),
            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 5.dp,
                bottomStart = 5.dp,
                bottomEnd = 5.dp
            ),) {
            Text("<-")
        }

        // column for each individual user card
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

            itemsIndexed(users) { index, user ->
                Spacer(modifier = Modifier.height(12.dp))
                AdminUserRow(user, userRepository)
            }
        }
    }
}

/**
 * this screen shows all the users that exist, their name, email, phone, and permission level,
 * and offers ban owner and delete user options
 * TODO: expand cards to show more info
 */
@Composable
fun AdminUserRow(userProfile: UserProfile, userRepository: UserRepository){



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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

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

                // buttons to delete and ban
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){

                    // if not trying to delete user, show the ban button
                    if (!confirmBan && userProfile.permission >= UserPermission.OWNER) {
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
                            onCancel = { confirmDeletion = false }
                        )
                    }

                    // if neither button is expanded put a space between them
                    if (!confirmBan && !confirmDeletion) {
                        Spacer(modifier = Modifier.size(10.dp))
                    }

                    // if not trying to ban user show the delete button
                    if (!confirmDeletion){
                        AdminActionOnUser(confirmBan, "Ban Owner",
                            onRequest = {
                                confirmDeletion = false
                                confirmBan = true
                            },
                            onConfirm = {
                                runBlocking { userRepository.banOwner(userProfile) }
                                confirmDeletion = false
                                deleted = true
                            },
                            onCancel = {confirmBan = false}
                        )
                    }
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
fun AdminActionOnUser(confirm: Boolean, action: String, onRequest: () -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit){

    // show the delete button if not AlreadyPressed
    if (!confirm) {
        Button(onRequest,

            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 5.dp,
                bottomStart = 5.dp,
                bottomEnd = 5.dp
            ),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF1493)),
            modifier = Modifier.size(height = 40.dp, width = 150.dp).testTag("get options")) {
            Text(action, textAlign = TextAlign.Center)
        }
    }

    // show the confirm and cancel buttons
    if (confirm) {
        Row() {

            //
            Button(onCancel,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                ),
                colors = ButtonDefaults.buttonColors(Color(0xFF008B8B)),
                modifier = Modifier.size(height = 40.dp, width = 150.dp).testTag("Cancel")) {
                Text("Cancel",textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.size(10.dp))

            Button(onConfirm,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                ),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF1493)),
                modifier = Modifier.size(height = 40.dp, width = 150.dp).testTag("Confirm")) {
                Text("Confirm", textAlign = TextAlign.Center)
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
        Button(onClick = {showButtons = true},
            modifier = Modifier.testTag("Admin Actions")){
            Text("Show Admin Actions")
        }
    }
    if (showButtons){

        Row() {

            Button(onClick = {showButtons = false},
                modifier = Modifier.testTag("Hide Actions")) {
                Text("Hide Admin Actions")
            }
            Button(onClick = {
                val user = runBlocking { userRepository.getUser(campsite.id) }

                if (user != null) {
                    runBlocking { userRepository.banOwner(user) }
                }
            },
                modifier = Modifier.testTag("Ban Owner")
            ) {
                Text("Ban Owner")
            }

            Button(onClick = {
                runBlocking { campsiteRepository.deleteCampsite(campsite.id) }
            },
                modifier = Modifier.testTag("Remove Campsite")
            ) {
                Text("Remove Listing")
            }

        }


    }
}