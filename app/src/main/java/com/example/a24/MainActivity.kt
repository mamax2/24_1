package com.example.a24

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.screens.HomeScreen
import com.example.a24.ui.screens.LoginScreen
import com.example.a24.ui.theme.AppTheme
import com.example.a24.App


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                    //HomeScreen()
                    //LoginScreen()
                }

            }
        }
    }
}
