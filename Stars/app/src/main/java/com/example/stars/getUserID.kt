package com.example.stars

import android.util.Log
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


import android.content.Context
import android.provider.Settings
import androidx.navigation.NavController
import kotlinx.coroutines.Deferred
/**
this function asks the android device the app is on for it's ID and then checks the firebase
userRepository to see if the android ID is in it. if it is then it sets the userProfile [currentUser]
to be the user in firebase. If not, then it creates a new user in firebase and sets the profile to
that instead.
BUG: (technically a feature) if there is no entry in firebase will not add new user.
    may also have problems with being offline
TODO: make it so that if you do not have an account you goto the edit profile page to make one
 */
fun getUserID(navController: NavController, userRepository: UserRepository, context: Context): UserProfile {

    // android ID that I don't really understand but it's fixed for the life of the device
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    // if this UserProfile hasn't changed then the device is new
    var currentUser = UserProfile(id = "check")

    // state in logs the deviceID
    Log.d("User ID Check", "Device ID: " + deviceId)

    // go over each user in the repo
    userRepository.users.forEach { user ->

        // if the local device ID is the same as the ID of the current repo user profile
        // then set the local user profile to be the repo user profile
        if (user.id == deviceId) {

            // show in log that a match was found
            Log.d("User ID Check", "user match found")
            currentUser = user
        }
    }

            // if currentUser was never re-assigned then it's a new account so create a new user
            // and add it to the user repo list
            if(currentUser.id == "check" && userRepository.users.isNotEmpty()){

                // show in log that this is a new device
                Log.d("User ID Check", "New Device")

                // create a new UserProfile and add it to the firebase repo
                currentUser = UserProfile(id = deviceId)
                userRepository.addOrUpdateUser(currentUser) // has been a problem forcing the app to not load
            }

    return currentUser
}
