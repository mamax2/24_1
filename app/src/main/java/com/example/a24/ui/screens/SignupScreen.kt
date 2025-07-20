package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightHighContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.managers.NotificationManager
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(navController: NavHostController){
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppBar(currentRoute = "signup", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Sign Up")

            Signup(navController)
        }
    }
}

@Composable
fun Signup(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Inizializza repository e notification manager
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }
    val notificationManager = remember { NotificationManager(repository) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(650.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // [Mantieni tutti i campi esistenti per nome, email, password, conferma password...]

            // Titolo "Name"
            Text(
                text = "Name",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Enter your name") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // [Altri campi...]
            Text(
                text = "Email",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter your email") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "Password",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "Confirm Password",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm your password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Pulsante "Sign Up" AGGIORNATO
            Button(
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            Toast.makeText(context, "Le password non coincidono", Toast.LENGTH_SHORT).show()
                        }
                        password.length < 6 -> {
                            Toast.makeText(context, "La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isLoading = true

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false

                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        user?.let { firebaseUser ->
                                            // Aggiorna il profilo Firebase con il nome
                                            val profileUpdates = userProfileChangeRequest {
                                                displayName = name
                                            }

                                            firebaseUser.updateProfile(profileUpdates)

                                            // Inizializza l'utente nel database locale
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    repository.initializeUser(
                                                        userId = firebaseUser.uid,
                                                        name = name,
                                                        email = email
                                                    )

                                                    // Invia notifiche di benvenuto per nuovi utenti
                                                    notificationManager.initializeNotificationsForNewUser(firebaseUser.uid)

                                                    // Award del primo badge
                                                    notificationManager.sendBadgeUnlockedNotification(
                                                        userId = firebaseUser.uid,
                                                        badgeName = "First Login",
                                                        badgeIcon = "🎉"
                                                    )
                                                } catch (e: Exception) {
                                                    // Log error
                                                }
                                            }
                                        }

                                        Toast.makeText(context, "Account creato con successo!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    } else {
                                        val errorMessage = task.exception?.message ?: "Errore durante la registrazione"
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightMediumContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = onPrimaryLightMediumContrast
                    )
                } else {
                    Text(text = "Sign Up")
                }
            }

            // Pulsante "Already have an account? Login"
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightHighContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text(text = "Already have an account? Login")
            }
        }
    }
}