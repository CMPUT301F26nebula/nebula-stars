package com.example.stars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController, currentUser: UserProfile){


}


@Composable
fun EditProfile(navController: NavController, currentUser: UserProfile){

    var newName by remember { mutableStateOf(currentUser.name) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = newName,
            onValueChange = {newName = it},
            label = { Text("Name") }
        )

        Button(onClick = {currentUser.name = newName}){Text("Save")}
    }
}