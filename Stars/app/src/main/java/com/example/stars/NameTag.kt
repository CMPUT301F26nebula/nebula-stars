/**
 * Clickable username tag
 */
package com.example.stars

import android.R.attr.onClick
import android.R.attr.text
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.stars.App.currentUser
import com.example.stars.App.navController
import kotlinx.coroutines.runBlocking

/**
 * Button showing the name of [user] that opens a dialogue with the option to message the user
 *
 * Entrants can only message owners, owners can message entrants and owners (as an entrant), and
 * admins can message anybody
 *
 * @param user user to display the name of
 * @param text Text composable to render the name
 */
@Composable
fun NameTag(user: UserProfile, text: @Composable ((String) -> Unit)) {
    var dialogState by remember { mutableStateOf(false) }
    val userColor = RandomStarsColor.fromId(user.id)
    val userContrast = RandomStarsColor.contrast(userColor)
    val navController = navController.current

    if (dialogState) {
        Dialog(
            onDismissRequest = { dialogState = false }
        ) {
            Card(
                colors = CardColors(
                    userColor,
                    userContrast,
                    userColor,
                    userContrast
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = user.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // entrants shouldnt be able to message other entrants
                        if (!(currentUser.permission == UserPermission.ENTRANT && user.permission == UserPermission.ENTRANT)) {
                            Button(
                                onClick = {
                                    val chat = runBlocking {
                                        Databases.chatRepository.getOrCreate(currentUser to user)
                                    }
                                    navController.navigate("messages/${chat.id}")
                                    dialogState = false
                                },
                                colors = ButtonColors(
                                    StarsColors.White,
                                    StarsColors.Purple,
                                    StarsColors.White,
                                    StarsColors.Purple
                                )
                            ) {
                                Text("Message")
                            }
                        }
                        Button(
                            onClick = {
                                dialogState = false
                            },
                            colors = ButtonColors(
                                StarsColors.White,
                                StarsColors.Purple,
                                StarsColors.White,
                                StarsColors.Purple
                            )
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
        },
        colors = ButtonColors(
            userColor,
            userContrast,
            userColor,
            userContrast
        )
    ) {
        text(user.name)
    }
}