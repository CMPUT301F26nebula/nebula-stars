/*
Token Manager for Firebase Messaging Service
Written by Gemini https://gemini.google.com/share/d/1qNVvsL0vRT0jLS_4x3j5z1WMtwIbrxcO?usp=sharing 2026-11-17
 */
package com.example.stars

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object PushTokenManager {

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String? {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.takeIf { it.isNotBlank() }
    }

    // Optional: Call in LaunchedEffect on app start or right after notification
    // permission is granted to guarantee the current FID is synced.
    suspend fun syncCurrentDevice(context: Context) {
        getAndroidId(context) ?: return
        FirebaseMessaging.getInstance().register().await()
        val fid = FirebaseInstallations.getInstance().id.await()
        saveFidForDevice(context, fid)
    }

    // Called automatically whenever FCM registers or rotates the FID
    fun saveFidForDevice(context: Context, fid: String) {
        val androidId = getAndroidId(context) ?: return
        // Only save if the user exists
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("id", androidId)
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                if (snapshot.count > 0) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document( androidId)
                        .update("firebaseId", fid)
                }
        }

    }
}