package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stars.ui.theme.ThemePresets
import kotlinx.coroutines.launch

/**
 * Lets the user pick a color theme, and their choice is saved in firestore
 */
@Composable
fun ThemesScreen(navController: NavController, currentUser: UserProfile, userRepository: UserRepository) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarsColors.Background)
            .navigationBarsPadding()
    ) {
        ScreenHeader(navController = navController, title = "Themes")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (preset in ThemePresets.all) {
                val selected = StarsColors.palette.name == preset.name

                Button(
                    onClick = {
                        StarsColors.palette = preset
                        currentUser.theme = preset.name
                        scope.launch { userRepository.addOrUpdate(currentUser) } //save
                    },
                    modifier = Modifier.size(width = 300.dp, height = 50.dp),
                    shape = RoundedCornerShape(
                        topStart = 5.dp,
                        topEnd = 5.dp,
                        bottomStart = 5.dp,
                        bottomEnd = 15.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) StarsColors.Highlight else StarsColors.Accent1,
                        contentColor = if (selected) StarsColors.OnHighlight else StarsColors.OnAccent
                    )
                ) {
                    Text(
                        text = if (selected) "✓ ${preset.name}" else preset.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}