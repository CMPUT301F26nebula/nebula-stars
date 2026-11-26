/**
 * Clickable username tag
 */
package com.example.stars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.example.stars.App.currentUser
import com.example.stars.App.navController
import kotlinx.coroutines.runBlocking

/**
 * Button showing the name of [user] that opens a dialogue with the option to message the user
 *
 * Entrants can only message owners, owners can message entrants and owners (as an entrant), and
 * admins can message anybody
 */
@Composable
fun NameTag(user: UserProfile) {
    var dialogState by remember { mutableStateOf(false) }
    val navController = navController.current

    if (dialogState) {
        Dialog(
            onDismissRequest = { dialogState = false }
        ) {
            Card() {
                Column() {
                    Text(user.name)
                    Row() {
                        if (
                            (currentUser.permission == UserPermission.ENTRANT && user.permission == UserPermission.OWNER) ||
                            (currentUser.permission == UserPermission.OWNER && user.permission <= UserPermission.OWNER) ||
                            (currentUser.permission == UserPermission.ADMIN)
                        ) {
                            Button(
                                onClick = {
                                    val chat = runBlocking {
                                        Databases.chatRepository.getOrCreate(currentUser to user)
                                    }
                                    navController.navigate("messages/${chat.id}")
                                    dialogState = false
                                }
                            ) {
                                Text("Message")
                            }
                        }
                    }
                    Row() {
                        Button(
                            onClick = {
                                dialogState = false
                            }
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }

    Button(
        onClick = {
            if (currentUser.id != user.id) {
                dialogState = true
            }
        }
    ) {
        Text(user.name)
    }
}