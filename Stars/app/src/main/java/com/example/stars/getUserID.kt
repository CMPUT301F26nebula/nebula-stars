package com.example.stars

import android.util.Log
import android.content.Context
import android.provider.Settings
import androidx.navigation.NavController
/**
this function asks the android device the app is on for it's ID and then checks the firebase
userRepository to see if the android ID is in it. if it is then it sets the userProfile [currentUser]
to be the user in firebase. If not, then it creates a new user in firebase and sets the profile to
that instead.
TODO: make it so that if you do not have an account you goto the edit profile page to make one
 */
suspend fun getUserID(navController: NavController, context: Context, userRepository: UserRepository): UserProfile {

    // android ID that I don't really understand but it's fixed for the life of the device
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    // state in logs the deviceID
    Log.d("User ID Check", "Device ID: " + deviceId)

    // find the user in the repo
    // if the local device ID is the same as the ID of the current repo user profile
    // then set the local user profile to be the repo user profile
    val storedUser = userRepository.getUser(deviceId)
    if (storedUser != null) {
        // show in log that a match was found
        Log.d("User ID Check", "user match found")
        return storedUser
    }

    // if currentUser was never re-assigned then it's a new account so create a new user
    // and add it to the user repo list
    // show in log that this is a new device
    Log.d("User ID Check", "New Device")

    // create a new UserProfile and add it to the firebase repo
    val currentUser = UserProfile(id = deviceId)
    userRepository.addOrUpdateUser(currentUser)

    return currentUser
}
