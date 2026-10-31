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

/*
navigate to the edit profile page
TODO: make it pretty and add notification settings and any others that are needed
 */
@Composable
fun SettingsScreen(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Button(onClick = { navController.navigate("edit profile") }) {
            Text("temp edit profile button")
        }
    }

}

/*
allows users to change name, email, phone
BUG: if you are offline and try to update something it will crash
    probably because addOrUpdateUser is runBlocking
TODO: make it pretty, ensure that name and email are provided
 */
@Composable
fun EditProfile(navController: NavController, currentUser: UserProfile, userRepository: UserRepository){

    // editable variables for text fields with the current values in them
    var newName by remember { mutableStateOf(currentUser.name) }
    var newEmail by remember { mutableStateOf(currentUser.email) }
    var newPhone by remember { mutableStateOf(currentUser.phone) }


    Box(contentAlignment = Alignment.Center) {

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.padding(50.dp))

            // text field to change name
            Text("NAME", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Name") }
            )

            Spacer(modifier = Modifier.padding(10.dp))

            // Text field to change email
            Text("EMAIL", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
            )
            Spacer(modifier = Modifier.padding(10.dp))

            // Text field to change phone number
            Text("PHONE", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newPhone,
                onValueChange = { newPhone = it },
            )

            Spacer(modifier = Modifier.padding(10.dp))

            Row(){
                // save new stuff button
                Button(onClick = {
                            // flag for if any changes actually happened
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

                            // if a change actually happened then update the firebase UserProfile
                            if (changeOccurred){
                                userRepository.addOrUpdateUser(currentUser)
                            }

                            // go back to the settings page
                            navController.popBackStack()
                        }
                ){
                    Text("Save", fontSize = 48.sp)
                }

                // button to go back
                Button(onClick = {navController.popBackStack()}){
                    Text("Cancel", fontSize = 48.sp)
                }
            }
        }
    }
}
