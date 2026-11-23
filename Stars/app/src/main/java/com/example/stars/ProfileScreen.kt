package com.example.stars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.navOptions
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.core.net.toUri
import com.google.apps.card.v1.Widget


/**
 * first shows edit and delete buttons, then if edit press allows users to change name, email, phone
 * and if delete press confirmation before sending you to make new profile
 * BUG: if you are offline and try to update something it will crash
 * probably because addOrUpdateUser is runBlocking
 */
@Composable
fun EditProfile(navController: NavController, currentUser: UserProfile, userRepository: UserRepository, imageRepository: ImageRepository){

    val imageId = remember { mutableStateListOf<String>() }

    // hides the delete and edit button and shows the confirm cancel buttons
    var confirm by remember {mutableStateOf(false)}

    // if you have not hit edit you can't change anything
    var readonlyDetails by remember { mutableStateOf(true) }

    // editable variables for text fields with the current values in them
    var newName by remember { mutableStateOf(currentUser.name) }
    var newEmail by remember { mutableStateOf(currentUser.email) }
    var newPhone by remember { mutableStateOf(currentUser.phone) }
    var newNotifs by remember { mutableStateOf(currentUser.notifs) }


    // if the image is not default then set the newImage value to the image Uri, otherwise just use null
    var newImage by remember { mutableStateOf<Uri?>(null ) }
    if (currentUser.profilePic != ""){
        newImage = runBlocking {imageRepository.getImageUri(currentUser.profilePic)}?.toUri()
    }

    // Used to launch updater function
    val scope = rememberCoroutineScope()

    // the label looks bad floating on top of the box so these are used to make it disappear if
    // is on top of the box
    var showNewNameLabel by remember { mutableStateOf(true) }
    var showNewEmailLabel by remember { mutableStateOf(true) }
    var showNewPhoneLabel by remember { mutableStateOf(true) }

    // labels to see if the field is selected
    // start as true for some reason
    var NewNameLabelFocused by remember { mutableStateOf(true) }
    var NewEmailLabelFocused by remember { mutableStateOf(true) }
    var NewPhoneLabelFocused by remember { mutableStateOf(true) }


    // remove labels if there is a value in the text field
    if (newName.isNotEmpty() || NewNameLabelFocused){
        showNewNameLabel = false
    }
    else{
        showNewNameLabel = true
    }
    if (newEmail.isNotEmpty() || NewEmailLabelFocused){
        showNewEmailLabel = false
    }else{
        showNewEmailLabel = true
    }
    if (newPhone.isNotEmpty() || NewPhoneLabelFocused){
        showNewPhoneLabel = false
    }else{
        showNewPhoneLabel = true
    }

    Column(Modifier.statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            BackButton(navController)
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    StarsColors.White,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 30.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                ).padding(10.dp)


            ) {

                Column(modifier = Modifier) {

                    // profile pic
                    val photoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri: Uri? ->
                        scope.launch {
                            if (uri != null) {
                                newImage = uri
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(100.dp, 100.dp).clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        enabled = !readonlyDetails,


                        ) {
                        AsyncImage(
                            model = newImage,
                            contentDescription = "profile picture",
                            modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
                            contentScale = ContentScale.Crop,
                            fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                            error = painterResource(R.drawable.default_pfp)
                        )
                    }


                    // text field to change name
                    Text(
                        "NAME",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.Purple
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = {
                            if (showNewNameLabel) {
                                Text("(Required)")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = StarsColors.Purple,
                            focusedContainerColor = StarsColors.Purple,
                            unfocusedTextColor = StarsColors.White,
                            focusedTextColor = StarsColors.White,
                            unfocusedLabelColor = StarsColors.White,
                            disabledTextColor = StarsColors.White,
                            disabledContainerColor = StarsColors.Purple
                        ),
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 30.dp
                        ),
                        modifier = Modifier.onFocusChanged(onFocusChanged = {
                            NewNameLabelFocused = !NewNameLabelFocused
                        })
                            .testTag("Name Field"),
                        enabled = !readonlyDetails
                    )

                    Spacer(modifier = Modifier.padding(10.dp))


                    /*
                taken from https://developer.android.com/develop/ui/compose/quick-guides/content/validate-input
                Author N/A, Last updated 2026-10-28 UTC
                 */
                    // Text field to change email
                    Text(
                        "EMAIL",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.Purple
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = {
                            if (showNewEmailLabel) {
                                Text("(Required)")
                            }
                        },
                        isError = !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                            .matches() && newEmail.isNotEmpty(),
                        supportingText = {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                                    .matches() && newEmail.isNotEmpty()
                            ) {
                                Text("Incorrect email format.")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = StarsColors.Purple,
                            focusedContainerColor = StarsColors.Purple,
                            unfocusedTextColor = StarsColors.White,
                            focusedTextColor = StarsColors.White,
                            unfocusedLabelColor = StarsColors.White,
                            errorContainerColor = StarsColors.Purple,
                            errorTextColor = StarsColors.White,
                            disabledTextColor = StarsColors.White,
                            disabledContainerColor = StarsColors.Purple
                        ),
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 30.dp
                        ),
                        modifier = Modifier.onFocusChanged(onFocusChanged = {
                            NewEmailLabelFocused = !NewEmailLabelFocused
                        })
                            .testTag("Email Field"),
                        enabled = !readonlyDetails
                    )

                    Spacer(modifier = Modifier.padding(10.dp))

                    /*
    taken from https://developer.android.com/develop/ui/compose/quick-guides/content/auto-format-phone-number
    Author N/A, Last updated 2026-10-28 UTC
     */
                    // Text field to change phone number
                    Text(
                        "PHONE",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.Purple
                    )
                    val numericRegex = Regex("[^0-9]")
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = {
                            // Remove non-numeric characters.
                            val stripped = numericRegex.replace(it, "")
                            newPhone = if (stripped.length >= 10) {
                                stripped.substring(0..9)
                            } else {
                                stripped
                            }
                        },
                        visualTransformation = NanpVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = {
                            if (showNewPhoneLabel) {
                                Text("(Optional)")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = StarsColors.Purple,
                            focusedContainerColor = StarsColors.Purple,
                            unfocusedTextColor = StarsColors.White,
                            focusedTextColor = StarsColors.White,
                            unfocusedLabelColor = StarsColors.White,
                            disabledTextColor = StarsColors.White,
                            disabledContainerColor = StarsColors.Purple,
                            disabledLabelColor = StarsColors.White

                        ),
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 30.dp
                        ),
                        modifier = Modifier.onFocusChanged(onFocusChanged = {
                            NewPhoneLabelFocused = !NewPhoneLabelFocused
                        })
                            .testTag("Phone Field"),
                        enabled = !readonlyDetails
                    )

                    Spacer(modifier = Modifier.padding(10.dp))

                    // Checkbox to toggle notifications
                    // From Gemini https://gemini.google.com/share/d/12V5TlyBwf4xR_JFURXXjZEklhkWv95LI?usp=sharing 2026-11-17
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "NOTIFICATIONS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.Purple
                        )
                        Checkbox(
                            checked = newNotifs,
                            onCheckedChange = { newNotifs = it },
                            enabled = !readonlyDetails,
                            colors = CheckboxDefaults.colors(
                                checkedColor = StarsColors.Purple,
                                uncheckedColor = StarsColors.Purple,
                                checkmarkColor = StarsColors.White,
                                disabledCheckedColor = StarsColors.Purple,
                                disabledUncheckedColor = StarsColors.Purple
                            ),
                            modifier = Modifier.testTag("Notifications Checkbox")
                        )
                    }

                    Spacer(modifier = Modifier.padding(10.dp))


                    if (readonlyDetails && !confirm) {
                        Row() {
                            Button(
                                onClick = { readonlyDetails = false },
                                modifier = Modifier.size(width = 150.dp, height = 50.dp)
                                    .testTag("Edit Button"),
                                shape = RoundedCornerShape(
                                    topStart = 5.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 0.dp
                                ),
                                colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                            ) {
                                Text("Edit", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.White)
                            }

                            Button(
                                onClick = { confirm = true },
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 5.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 15.dp
                                ),
                                modifier = Modifier.size(width = 150.dp, height = 50.dp)
                                    .testTag("Delete Button"),
                                colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                            ) { Text("Delete", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.White) }
                        }
                    }

                    if (readonlyDetails && confirm) {

                        ConfirmCancelButtons(
                            onConfirm = {
                                scope.launch {
                                    userRepository.deleteUser(currentUser)
                                    navController.navigate(
                                        "new profile",
                                        // deletes all the past history of the back stack so that you can't back out of make account
                                        navOptions = navOptions {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    )
                                }
                            },
                            onCancel = { confirm = false }
                        )
                    }

                    if (!readonlyDetails) {
                        Row() {

                            // save new stuff button
                            Button(
                                onClick = {
                                    // flag for if any changes actually happened
                                    var changeOccurred = false

                                    if (newName != currentUser.name) {
                                        currentUser.name = newName
                                        changeOccurred = true
                                    }

                                    if (newEmail != currentUser.email) {
                                        currentUser.email = newEmail
                                        changeOccurred = true
                                    }

                                    if (newPhone != currentUser.phone) {
                                        currentUser.phone = newPhone
                                        changeOccurred = true
                                    }

                                    if (newNotifs != currentUser.notifs) {
                                        currentUser.notifs = newNotifs
                                        changeOccurred = true
                                    }

                                    // get current image uri
                                    val image = newImage
                                    // check if the profile picture is not default because getImageUri crashes when it is
                                    if (currentUser.profilePic != "") {

                                        // check to ensure the images are not the same and that the image is not null
                                        if (image != Uri.parse(runBlocking {
                                                imageRepository.getImageUri(
                                                    currentUser.profilePic
                                                )
                                            })
                                            && image != null
                                        ) {

                                            // delete the old image and add the new one
                                            imageRepository.deleteImage(currentUser.profilePic)
                                            currentUser.profilePic =
                                                runBlocking { imageRepository.uploadImage(image) }

                                            changeOccurred = true
                                        }
                                    }

                                    // if the profile picture is default then just check null
                                    else if (image != null) {
                                        currentUser.profilePic =
                                            runBlocking { imageRepository.uploadImage(image) }

                                        changeOccurred = true
                                    }


                                    // if a change actually happened then update the firebase UserProfile
                                    if (changeOccurred) {
                                        scope.launch {
                                            userRepository.addOrUpdateUser(currentUser)
                                        }
                                    }

                                    // go back to the settings page
                                    navController.popBackStack()
                                },
                                enabled = android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                                    .matches()
                                        && (newPhone.length == 10 || newPhone.isEmpty()) && newName.isNotEmpty(),

                                shape = RoundedCornerShape(
                                    topStart = 5.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 0.dp
                                ),
                                modifier = Modifier.size(width = 150.dp, height = 50.dp)
                                    .testTag("Save Edits"),
                                colors = ButtonDefaults.buttonColors(StarsColors.Cyan)

                            ) {
                                Text("Save", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.White)
                            }

                            // button to go back
                            Button(
                                onClick = {
                                    readonlyDetails = true
                                },

                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 5.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 15.dp
                                ),
                                modifier = Modifier.size(width = 150.dp, height = 50.dp)
                                    .testTag("Cancel Edits"),
                                colors = ButtonDefaults.buttonColors(StarsColors.Magenta),
                            ) {
                                Text("Cancel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarsColors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * on start or on delete, this screen pop's up so that you enter your name and email
 * this screen blocks you from going anywhere until you make your new account
 */
@Composable
fun MakeNewProfile(navController: NavController, currentUser: UserProfile, userRepository: UserRepository, imageRepository: ImageRepository){


    // editable variables for text fields with the current values in them
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newNotifs by remember { mutableStateOf(currentUser.notifs) }

    // if the image is not default then set the newImage value to the image Uri, otherwise just use null
    var newImage by remember { mutableStateOf<Uri?>(null ) }
    if (currentUser.profilePic != ""){
        newImage = runBlocking {imageRepository.getImageUri(currentUser.profilePic)}?.toUri()
    }

    // used to hide the label
    var showNewNameLabel by remember { mutableStateOf(true) }
    var showNewEmailLabel by remember { mutableStateOf(true) }
    var showNewPhoneLabel by remember { mutableStateOf(true) }

    // labels to see if the field is selected
    // start as true for some reason
    var NewNameLabelFocused by remember { mutableStateOf(true) }
    var NewEmailLabelFocused by remember { mutableStateOf(true) }
    var NewPhoneLabelFocused by remember { mutableStateOf(true) }


    // Used to launch updater function
    val scope = rememberCoroutineScope()

    // remove labels if there is a value in the text field
    if (newName.isNotEmpty() || NewNameLabelFocused){
        showNewNameLabel = false
    }
    else{
        showNewNameLabel = true
    }
    if (newEmail.isNotEmpty() || NewEmailLabelFocused){
        showNewEmailLabel = false
    }else{
        showNewEmailLabel = true
    }
    if (newPhone.isNotEmpty() || NewPhoneLabelFocused){
        showNewPhoneLabel = false
    }else{
        showNewPhoneLabel = true
    }

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                StarsColors.White,
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 30.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            ).padding(10.dp)


        ) {

            Column(modifier = Modifier) {

                // profile pic
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    scope.launch {
                        if (uri != null) {
                            newImage = uri
                        }
                    }
                }

                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.size(100.dp, 100.dp).clip(CircleShape)
                        .align(Alignment.CenterHorizontally),


                    ) {
                    AsyncImage(
                        model = newImage,
                        contentDescription = "profile picture",
                        modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                        error = painterResource(R.drawable.default_pfp)
                    )
                }

                // text field to change name
                Text(
                    "NAME",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsColors.Purple
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = {
                        if (showNewNameLabel) {
                            Text("(Required)")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = StarsColors.Purple,
                        focusedContainerColor = StarsColors.Purple,
                        unfocusedTextColor = StarsColors.White,
                        focusedTextColor = StarsColors.White,
                        unfocusedLabelColor = StarsColors.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 30.dp
                    ),
                    modifier = Modifier.onFocusChanged(onFocusChanged = {
                        NewNameLabelFocused = !NewNameLabelFocused
                    })
                        .testTag("Name Field")
                )

                Spacer(modifier = Modifier.padding(10.dp))


                /*
        taken from https://developer.android.com/develop/ui/compose/quick-guides/content/validate-input
        Author N/A, Last updated 2026-10-28 UTC
         */
                // Text field to change email
                Text(
                    "EMAIL",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsColors.Purple
                )
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = {
                        if (showNewEmailLabel) {
                            Text("(Required)")
                        }
                    },
                    isError = !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                        .matches() && newEmail.isNotEmpty(),
                    supportingText = {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail)
                                .matches() && newEmail.isNotEmpty()
                        ) {
                            Text("Incorrect email format.")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = StarsColors.Purple,
                        focusedContainerColor = StarsColors.Purple,
                        unfocusedTextColor = StarsColors.White,
                        focusedTextColor = StarsColors.White,
                        unfocusedLabelColor = StarsColors.White,
                        errorContainerColor = StarsColors.Purple,
                        errorTextColor = StarsColors.White,
                    ),
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 30.dp
                    ),
                    modifier = Modifier.onFocusChanged(onFocusChanged = {
                        NewEmailLabelFocused = !NewEmailLabelFocused
                    })
                        .testTag("Email Field")
                )

                Spacer(modifier = Modifier.padding(10.dp))

                /*
        taken from https://developer.android.com/develop/ui/compose/quick-guides/content/auto-format-phone-number
        Author N/A, Last updated 2026-10-28 UTC
         */
                // Text field to change phone number
                Text(
                    "PHONE",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsColors.Purple
                )
                val numericRegex = Regex("[^0-9]")
                OutlinedTextField(
                    value = newPhone,
                    onValueChange = {
                        // Remove non-numeric characters.
                        val stripped = numericRegex.replace(it, "")
                        newPhone = if (stripped.length >= 10) {
                            stripped.substring(0..9)
                        } else {
                            stripped
                        }
                    },
                    visualTransformation = NanpVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        if (showNewPhoneLabel) {
                            Text("(Optional)")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = StarsColors.Purple,
                        focusedContainerColor = StarsColors.Purple,
                        unfocusedTextColor = StarsColors.White,
                        focusedTextColor = StarsColors.White,
                        unfocusedLabelColor = StarsColors.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 30.dp
                    ),
                    modifier = Modifier.onFocusChanged(onFocusChanged = {
                        NewPhoneLabelFocused = !NewPhoneLabelFocused
                    })
                        .testTag("Phone Field")
                )

                Spacer(modifier = Modifier.padding(10.dp))

                // Checkbox to toggle notifications
                // From Gemini https://gemini.google.com/share/d/12V5TlyBwf4xR_JFURXXjZEklhkWv95LI?usp=sharing 2026-11-17
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "NOTIFICATIONS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.Purple
                    )
                    Checkbox(
                        checked = newNotifs,
                        onCheckedChange = { newNotifs = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = StarsColors.Purple,
                            uncheckedColor = StarsColors.Purple,
                            checkmarkColor = StarsColors.White
                        ),
                        modifier = Modifier.testTag("Notifications Checkbox")
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Row() {
                    // save new stuff button
                    Button(
                        onClick = {
                            // flag for if any changes actually happened
                            var changeOccurred = false

                            if (newName != currentUser.name) {
                                currentUser.name = newName
                                changeOccurred = true
                            }

                            if (newEmail != currentUser.email) {
                                currentUser.email = newEmail
                                changeOccurred = true
                            }

                            if (newPhone != currentUser.phone) {
                                currentUser.phone = newPhone
                                changeOccurred = true
                            }

                            if (newNotifs != currentUser.notifs) {
                                currentUser.notifs = newNotifs
                                changeOccurred = true
                            }

                            // get current image uri
                            val image = newImage
                            // check if the profile picture is not default because getImageUri crashes when it is
                            if (currentUser.profilePic != "") {

                                // check to ensure the images are not the same and that the image is not null
                                if (image != Uri.parse(runBlocking {
                                        imageRepository.getImageUri(
                                            currentUser.profilePic
                                        )
                                    })
                                    && image != null
                                ) {

                                    // delete the old image and add the new one
                                    imageRepository.deleteImage(currentUser.profilePic)
                                    currentUser.profilePic =
                                        runBlocking { imageRepository.uploadImage(image) }

                                    changeOccurred = true
                                }
                            }

                            // if the profile picture is default then just check null
                            else if (image != null) {
                                currentUser.profilePic =
                                    runBlocking { imageRepository.uploadImage(image) }

                                changeOccurred = true
                            }

                            // if a change actually happened then update the firebase UserProfile
                            if (changeOccurred) {
                                scope.launch {
                                    userRepository.addOrUpdateUser(currentUser)
                                }
                            }

                            // go back to the settings page
                            navController.navigate(
                                "home",
                                // wipe all previous backs so you can't back into this
                                navOptions = navOptions {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            )

                        },
                        enabled = android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
                                && (newPhone.length == 10 || newPhone.isEmpty()) && newName.isNotEmpty(),

                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            topEnd = 5.dp,
                            bottomStart = 5.dp,
                            bottomEnd = 5.dp
                        ),
                        modifier = Modifier.size(width = 300.dp, height = 50.dp).testTag("Confirm"),
                        colors = ButtonDefaults.buttonColors(StarsColors.Cyan)
                    ) {
                        Text("Confirm", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = StarsColors.White)
                    }
                }
            }
        }
    }
}