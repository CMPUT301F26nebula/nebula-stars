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
// TODO: implement account settings data class
/**
 * A profile for a user registered to the app
 *
 * @param id the unique ID of the user
 * @param name the user's human-readable name
 * @param email the user's email address
 * @param permission the app permission level for the user
 */
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val permission: UserPermission = UserPermission.ENTRANT
)
