/*
Contains classes underpinning the user profile, including their permissions and account settings

TODO: Add and implement data class for account settings
 */
package com.example.stars

/**
 * The possible permission levels a user can have
 *
 * Higher constant ordinals correspond to extending permission levels,
 * e.g. a user with permission level ADMIN is assumed to also have permission level OWNER
 */
enum class UserPermission {
    ENTRANT, OWNER, ADMIN
}
/**
 * A profile for a user registered to the app
 *
 * @param id the unique ID of the user
 * @param name the user's human-readable name
 * @param email the user's email address
 * @param phone the user's phone number currently stored as a string
 * @param permission the app permission level for the user
 */
data class UserProfile(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var permission: UserPermission = UserPermission.ENTRANT

)
