package com.example.stars

import android.widget.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking

@Composable
fun AdminViewUserProfiles(navController: NavController, userRepository: UserRepository){

    val users = runBlocking { userRepository.getUsersList() }




    Column(modifier = Modifier.fillMaxSize()) {

        Button(onClick = { navController.popBackStack() },
            modifier = Modifier.systemBarsPadding(),
            colors = ButtonDefaults.buttonColors(Color(0xFF008B8B)),
            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 5.dp,
                bottomStart = 5.dp,
                bottomEnd = 5.dp
            ),) {
            Text("<-")
        }

        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

            itemsIndexed(users) { index, user ->
                Spacer(modifier = Modifier.height(12.dp))
                AdminUserRow(user, userRepository)
            }
        }
    }
}

@Composable
fun AdminUserRow(userProfile: UserProfile, userRepository: UserRepository){
    var permissionLevel by remember { mutableStateOf("Entrant") }
    var confirmDeletion by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    if (userProfile.permission == UserPermission.ADMIN){
        permissionLevel = "Admin"
    }
    if (userProfile.permission == UserPermission.OWNER){
        permissionLevel = "Owner"
    }

    if (!deleted) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Campsite text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Name
                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (!confirmDeletion) {
                        Button(onClick = { confirmDeletion = true },

                            shape = RoundedCornerShape(
                                    topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 5.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFFEFC700))) {
                            Text("Delete User")
                        }
                    }
                    if (confirmDeletion) {
                        Column() {

                            Button(onClick = {confirmDeletion = false},
                                shape = RoundedCornerShape(
                                    topStart = 5.dp,
                                    topEnd = 5.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 5.dp
                                ),
                                colors = ButtonDefaults.buttonColors(Color(0xFF008B8B))) {
                                Text("Cancel Delete")
                            }

                            Button(onClick = {
                                runBlocking {
                                    userRepository.deleteUser(userProfile)
                                    confirmDeletion = false
                                    deleted = true
                                }
                            },
                                shape = RoundedCornerShape(
                                    topStart = 5.dp,
                                    topEnd = 5.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 5.dp
                                ),
                                colors = ButtonDefaults.buttonColors(Color(0xFFFF1493))) {
                                Text("Confirm Delete")
                            }


                        }

                    }

                }
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

                val nanpVisualTransformation = NanpVisualTransformation()

                // Description
                Text(
                    text = nanpVisualTransformation.filter(AnnotatedString(userProfile.phone)).text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}


/*
shows on campsite card. Allows admins to ban owners and remove campsite listings
banning owners demotes them to ENTRANTS
TODO: notify owner that they have been banned
TODO: notify owner and anyone in a lottery for this site that it has been removed
 */
@Composable
fun AdminCampsiteActions(campsite: Campsite, userRepository: UserRepository, campsiteRepository: CampsiteRepository){
    var showButtons by remember { mutableStateOf(false) }

    if (!showButtons){
        Button(onClick = {showButtons = true}){
            Text("Show Admin Actions")
        }
    }
    if (showButtons){

        Column() {

            Button(onClick = {showButtons = false}) {
                Text("Hide Admin Actions")
            }
            Button(onClick = {
                    val banUser = runBlocking { userRepository.getUser(campsite.ownerId) }
                    val ownedCampsites = runBlocking { campsiteRepository.getUserCampsites(campsite.ownerId) }
                    for (camp in ownedCampsites){
                        runBlocking { campsiteRepository.deleteCampsite(camp.id) }
                    }

                if (banUser != null) {
                    banUser.banned = true
                    banUser.permission = UserPermission.ENTRANT

                    runBlocking { userRepository.addOrUpdateUser(banUser) }
                }
            }
            ) {
                Text("Ban Owner")
            }

            Button(onClick = {
                runBlocking { campsiteRepository.deleteCampsite(campsite.id) }
                }
            ) {
                Text("Remove Listing")
            }

        }


    }
}