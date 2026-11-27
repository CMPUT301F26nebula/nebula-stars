package com.example.stars

import android.transition.Scene
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun AppPolicyScreen(navController: NavController){

    ScreenHeader(navController = navController, title = "POLICIES & GUIDELINES")

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()) {


        //TODO: change out color
        Card(
            modifier = Modifier.size(height = 500.dp, width = 310.dp).padding(10.dp),

        shape = RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 30.dp,
            bottomStart = 10.dp,
            bottomEnd = 10.dp
        ),
            //TODO: change out color
            colors  = CardDefaults.cardColors(
                containerColor = StarsColors.White
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    text = "App Policies & Guidelines",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsColors.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Policy Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsColors.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The app policies and guidelines will appear here once they have been approved by the Nebula Stars team.",
                    fontSize = 16.sp,
                    color = StarsColors.Black,
                    modifier = Modifier.testTag("policyContent")
                )

            }
        }
    }
}