/**
 * Clickable username tag
 */
package com.example.stars

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.navOptions
import com.example.stars.App.navController

/**
 * Label showing a [name] that redirects to the user profile, always underlined for consistency
 *
 * @param name name of user
 * @param id user id
 * @param modifier composable modifiers
 * @param style text styling options, same as in the [Text] composable
 * @param label optional leading text
 * @param navRoute if different from "user profile", this value will override it
 */
@Composable
fun NameTag(
    name: String,
    id: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    label: String = "",
    navRoute: String = "user profile"
) {
    val navController = navController.current
    if (label.isNotBlank()) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = style
        )
    }
    Text(
        text = name,
        textDecoration = TextDecoration.Underline,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = style,
        modifier = modifier.clickable(
            onClick = {
                navController.navigate("$navRoute/${Uri.encode(id)}", navOptions {
                })
            }
        )
    )
}