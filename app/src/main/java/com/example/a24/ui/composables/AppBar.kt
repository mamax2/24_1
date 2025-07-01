package com.example.a24.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.a24.ui.theme.ColorFamily
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.primaryContainerLight
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.theme.secondaryContainerLight
import com.example.a24.ui.theme.secondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentRoute: String? = null,
    navController: NavHostController? = null
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = primaryLight,
            titleContentColor = onPrimaryLight,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight
        ),
        title = {
            Text(
                "24+1",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 42.sp)
            )
        },
        navigationIcon = {
            // Mostra navigation icon SOLO se NON sei in login, signup o home
            if (currentRoute != null && currentRoute !in listOf("login", "signup", "home")) {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Mostra actions SOLO se NON sei in login o signup
            if (currentRoute != null && currentRoute !in listOf("login", "signup")) {
                IconButton(onClick = {
                    navController?.navigate("notifications")
                }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )

                }
                IconButton(onClick = {
                    navController?.navigate("profile")
                }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile"
                    )
                }
            }
        }
    )
}