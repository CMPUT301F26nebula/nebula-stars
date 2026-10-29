package com.example.stars

import android.util.Log
import com.google.firebase.installations.FirebaseInstallations

fun getIDOnStart(userRepository: UserRepository): UserProfile {

    var currentUser: UserProfile? = null
    /**
     * taken from the firebase website https://firebase.google.com/docs/projects/manage-installations?utm_source=google&utm_medium=cpc&utm_campaign=Cloud-SS-DR-Firebase-FY26-global-gsem-1713590&utm_content=text-ad&utm_term=KW_firebase&gad_source=1&gad_campaignid=23417478209&gbraid=0AAAAADpUDOi4qBEUiNLxYdYJU3glALvaT&gclid=CjwKCAjwuobXBhBXEiwAYUaIldXIUqO1-_xSoHG19ncJXyuhYvLieWuGTE500Fjld2hxvPmAeiOnQxoCoRMQAvD_BwE#kotlin
     * no specified author Last updated 2026-10-28 UTC
     */
    // this gets the device ID from firebase
    FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Installations", "Installation ID: " + task.result)

            // check if the user ID already exists
            userRepository.users.forEach { user ->
                if (user.id == task.result) {
                    currentUser = user
                }
            }

            // if currentUser was never assigned then it's a new account so create a new user
            // and add it to the user repo list
            if(currentUser == null){
                currentUser = UserProfile(id = task.result)
                userRepository.addOrUpdateUser(currentUser!!)
            }


        } else {
            Log.e("Installations", "Unable to get Installation ID")
        }

    }

    if(currentUser == null){
        currentUser = UserProfile(id = "test")
        userRepository.addOrUpdateUser(currentUser)
    }

    return currentUser
}