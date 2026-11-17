/*
An extended firebase messaging service for push notifications
Written by Gemini https://gemini.google.com/share/d/1qNVvsL0vRT0jLS_4x3j5z1WMtwIbrxcO?usp=sharing 2026-11-17
 */
package com.example.stars

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StarsFirebaseMessagingService : FirebaseMessagingService() {
    // Called when the app instance registers with FCM or its FID rotates.
    // Send this ID to Firestore or your backend so your Cloud Function can target this device.
    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        PushTokenManager.saveFidForDevice(this, installationId)
    }

    // Called when a message arrives while the app is in the foreground,
    // or whenever a data-only payload arrives in the background/foreground.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle foreground notifications or data payloads
    }
}