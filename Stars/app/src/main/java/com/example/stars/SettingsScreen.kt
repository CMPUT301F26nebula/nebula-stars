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
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    val scope = rememberCoroutineScope()

    var showOwnerSignUp by remember { mutableStateOf(false) }
    var showAdminSignUp by remember { mutableStateOf(false) }
    var showAdminRemove by remember { mutableStateOf(false) }
    var showOwnerRemove by remember { mutableStateOf(false) }


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


    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                Color(0xFFFFFFFF),
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 30.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            ).padding(10.dp).size(height = 500.dp, width = 310.dp)
        )
        {


            Column() {

                if (currentUser.permission != UserPermission.ADMIN && !showAdminSignUp) {
                    Button(
                        onClick = {
                            showAdminSignUp = true
                            showOwnerSignUp = false
                            showOwnerRemove = false
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Request Admin"),
                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            topEnd = 5.dp,
                            bottomStart = 5.dp,
                            bottomEnd = 15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFFEFC700)),
                        enabled = !currentUser.banned
                    ) { Text("Request Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }

                // very much a stop gap measures for now. Probably will notify admins the request later
                if (showAdminSignUp) {
                    Button(
                        onClick = {
                            currentUser.permission = UserPermission.ADMIN
                            scope.launch {
                                userRepository.addOrUpdateUser(currentUser)
                                showAdminSignUp = false
                            }
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Become Admin"),
                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            topEnd = 5.dp,
                            bottomStart = 5.dp,
                            bottomEnd = 15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFF008B8B))
                    ) {
                        Text("Confirm Become Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentUser.permission == UserPermission.ADMIN && !showAdminRemove) {
                    Button(
                        onClick = { showAdminRemove = true },
                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            topEnd = 5.dp,
                            bottomStart = 5.dp,
                            bottomEnd = 5.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFF1493))
                    ) {
                        Text(
                            "Remove Admin Status",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showAdminRemove) {
                    ConfirmCancelButtons(
                        {
                            currentUser.permission = UserPermission.OWNER
                            scope.launch {
                                userRepository.addOrUpdateUser(currentUser)
                            }
                            showAdminRemove = false
                        },
                        { showAdminRemove = false }
                    )
                }

                if (currentUser.permission < UserPermission.OWNER && !showOwnerSignUp) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            showOwnerSignUp = true
                            showAdminSignUp = false
                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Become Owner"),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFFEFC700)),
                        enabled = !currentUser.banned
                    ) {
                        Text("Become Owner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showOwnerSignUp) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            currentUser.permission = UserPermission.OWNER
                            scope.launch {
                                userRepository.addOrUpdateUser(currentUser)
                            }
                            showOwnerSignUp = false

                        },
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                            .testTag("Become Owner"),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFF008B8B))
                    ) {
                        Text("Confirm Become Owner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentUser.permission == UserPermission.OWNER && !showOwnerRemove) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = {
                            showOwnerRemove = true
                            showAdminSignUp = false
                        },
                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            topEnd = 5.dp,
                            bottomStart = 5.dp,
                            bottomEnd = 15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFF1493)),
                        modifier = Modifier.size(width = 300.dp, height = 50.dp)
                    ) {
                        Text(
                            "Remove Owner Status",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }


                if (showOwnerRemove) {
                    Spacer(modifier = Modifier.size(10.dp))
                    ConfirmCancelButtons(
                        {
                            currentUser.permission = UserPermission.ENTRANT
                            scope.launch {
                                userRepository.addOrUpdateUser(currentUser)
                            }
                            showOwnerRemove = false
                        },
                        { showOwnerRemove = false }
                    )
                }
            }
        }
    }

}

@Composable
fun ConfirmCancelButtons(onConfirm: () -> Unit, onCancel: () -> Unit){

    Row (verticalAlignment = Alignment.Bottom) {
        Button(onConfirm,

            modifier = Modifier.size(width = 150.dp, height = 50.dp).testTag("Confirm").align(alignment = Alignment.CenterVertically),
            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 0.dp,
                bottomStart = 5.dp,
                bottomEnd = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF1493))
        ) {
            Text("Confirm", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF))
        }


        Button(onCancel,
            modifier = Modifier.size(width = 150.dp, height = 50.dp).testTag("Cancel").align(alignment = Alignment.CenterVertically),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 5.dp,
                bottomStart = 0.dp,
                bottomEnd = 15.dp
            ),
            colors = ButtonDefaults.buttonColors(Color(0xFF008B8B))
        ){
            Text("Cancel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF))
        }
    }
}
