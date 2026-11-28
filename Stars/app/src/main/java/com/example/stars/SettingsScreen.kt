package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

/**
 * Screen you can get to from the home page, shows all your settings options as well as register as
 * owner and become admin.
 * TODO: make it so that admins are notified on admin request to approve new admins
 * TODO: any other settings we need
 */
@Composable
fun SettingsScreen(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    val scope = rememberCoroutineScope()

    // expose sign up buttons
    var showOwnerSignUp by remember { mutableStateOf(false) }
    var showAdminSignUp by remember { mutableStateOf(false) }

    // expose removal buttons
    var showAdminRemove by remember { mutableStateOf(false) }
    var showOwnerRemove by remember { mutableStateOf(false) }


    // back button
    ScreenHeader(navController = navController, title = "SETTINGS")

    // used for centering
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // white box in middle which holds all buttons
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                StarsColors.Surface,
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 30.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            ).padding(10.dp).size(height = 500.dp, width = 310.dp)
        )
        {

            // button holding column
            Column() {
                // themes
                Button(
                    onClick = { navController.navigate("themes") },
                    modifier = Modifier.size(width = 300.dp, height = 50.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarsColors.Accent3,
                        contentColor = StarsColors.OnAccent
                    )
                ) { Text("Themes", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.size(10.dp))

                // if has not clicked anything, show first become admin button
                if (currentUser.permission != UserPermission.ADMIN && !showAdminSignUp) {
                    Button(
                        onClick = {
                            showAdminSignUp = true
                            showOwnerSignUp = false
                            showOwnerRemove = false
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Request Admin"),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Accent3, StarsColors.OnAccent),
                        enabled = !currentUser.banned
                    ) { Text("Request Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }

                // confirm become and admin
                // very much a stop gap measures for now. Probably will notify admins the request later
                if (showAdminSignUp) {
                    Button(
                        onClick = {
                            currentUser.permission = UserPermission.ADMIN
                            scope.launch {
                                userRepository.addOrUpdate(currentUser)
                                showAdminSignUp = false
                            }
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Become Admin"),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Cancel, StarsColors.OnAccent)
                    ) {
                        Text("Confirm Become Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // if you are an admin, show the remove admin status button
                if (currentUser.permission == UserPermission.ADMIN && !showAdminRemove) {
                    Button(
                        onClick = { showAdminRemove = true },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Confirm, StarsColors.OnAccent),
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                    ) {
                        Text(
                            "Remove Admin Status",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // confirm remove admin status
                if (showAdminRemove) {
                    ConfirmCancelButtons(
                        {
                            currentUser.permission = UserPermission.OWNER
                            scope.launch {
                                userRepository.addOrUpdate(currentUser)
                            }
                            showAdminRemove = false
                        },
                        { showAdminRemove = false }
                    )
                }

                // if you are not an owner or admin, sign up to be an owner
                if (currentUser.permission < UserPermission.OWNER && !showOwnerSignUp) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            showOwnerSignUp = true
                            showAdminSignUp = false
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Become Owner"),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Accent3, StarsColors.OnAccent),
                        enabled = !currentUser.banned
                    ) {
                        Text("Become Owner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // confirm becoming owner
                if (showOwnerSignUp) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            currentUser.permission = UserPermission.OWNER
                            scope.launch {
                                userRepository.addOrUpdate(currentUser)
                            }
                            showOwnerSignUp = false

                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Confirm Become Owner"),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Cancel, StarsColors.OnAccent)
                    ) {
                        Text("Confirm Become Owner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // if you are an owner, remove owner status
                if (currentUser.permission == UserPermission.OWNER && !showOwnerRemove) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            showOwnerRemove = true
                            showAdminSignUp = false
                        },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(StarsColors.Confirm, StarsColors.OnAccent),
                        modifier = Modifier.size(width = 300.dp, height = 50.dp).testTag("Remove Owner")
                    ) {
                        Text(
                            "Remove Owner Status",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                // confirm remove owner status
                if (showOwnerRemove) {
                    Spacer(modifier = Modifier.size(10.dp))
                    ConfirmCancelButtons(
                        {
                            currentUser.permission = UserPermission.ENTRANT
                            scope.launch {
                                userRepository.addOrUpdate(currentUser)
                            }
                            showOwnerRemove = false
                        },
                        { showOwnerRemove = false }
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                // user story #58 - allow users to view app policies and guidelines

                Button(
                    onClick = {
                        navController.navigate("app policies")
                    },
                    modifier = Modifier
                        .size(width = 300.dp, height = 50.dp)
                        .testTag("appPolicyButton"),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarsColors.Accent1
                    )
                ) {
                    Text(
                        text = "Policies & Guidelines",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.OnBackground
                    )
                }
            }
        }
    }
}

/**
 * buttons for confirming status changes
 */
@Composable
fun ConfirmCancelButtons(onConfirm: () -> Unit, onCancel: () -> Unit){

    Row (verticalAlignment = Alignment.Bottom) {

        // confirm something button
        Button(onConfirm,

            modifier = Modifier.size(width = 150.dp, height = 50.dp).testTag("Confirm").align(alignment = Alignment.CenterVertically),
            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 0.dp,
                bottomStart = 5.dp,
                bottomEnd = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(StarsColors.Confirm)
        ) {
            Text("Confirm", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.OnAccent)
        }


        // cancel something button
        Button(onCancel,
            modifier = Modifier.size(width = 150.dp, height = 50.dp).testTag("Cancel").align(alignment = Alignment.CenterVertically),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 5.dp,
                bottomStart = 0.dp,
                bottomEnd = 15.dp
            ),
            colors = ButtonDefaults.buttonColors(StarsColors.Cancel)
        ){
            Text("Cancel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.OnAccent)
        }
    }
}
