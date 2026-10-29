package com.example.stars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    Button(onClick = {navController.navigate("edit profile")}){
        Text("temp edit profile button")
    }

}


@Composable
fun EditProfile(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    var newName by remember { mutableStateOf(currentUser.name) }
    var newEmail by remember { mutableStateOf(currentUser.email) }
    var newPhone by remember { mutableStateOf(currentUser.phone) }

    Box(contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text("USERNAME", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Name") }
            )

            Spacer(modifier = Modifier.padding(10.dp))

            Text("EMAIL", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
            )
            Spacer(modifier = Modifier.padding(10.dp))

            Text("PHONE", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newPhone,
                onValueChange = { newPhone = it },
            )

            Spacer(modifier = Modifier.padding(10.dp))

            Row(){
                Button(onClick = {
                            var changeOccurred = false
                            if (newName != currentUser.name){
                                currentUser.name = newName
                                changeOccurred = true
                            }

                            if (newEmail != currentUser.email){
                                currentUser.email = newEmail
                                changeOccurred = true
                            }

                            if (newPhone != currentUser.phone){
                                currentUser.phone =  newPhone
                                changeOccurred = true
                            }

                            if (changeOccurred){
                                userRepository.addOrUpdateUser(currentUser)
                            }

                            navController.popBackStack()
                        }
                ){
                    Text("Save", fontSize = 48.sp)
                }

                Button(onClick = {navController.popBackStack()}){
                    Text("Cancel", fontSize = 48.sp)
                }
            }
        }
    }
}
