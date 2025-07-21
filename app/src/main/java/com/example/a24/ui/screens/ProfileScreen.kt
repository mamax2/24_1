package com.example.a24.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
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
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// Data class per i badge
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val color: Color
)

@Composable
fun ProfileScreen(navController: NavHostController) {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "profile", navController = navController)
            SectionHeader(text = "Profile")
            ProfileContent(navController)
        }
    }
}

@Composable
fun ProfileContent(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scrollState = rememberScrollState()

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                val savedImagePath = saveImageToAppStorage(context, uri)
                if (savedImagePath != null) {
                    profileImageUrl = savedImagePath
                    currentUser?.let { user ->
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.updateUserProfile(
                                userId = user.uid,
                                name = name,
                                profileImageUrl = savedImagePath
                            )
                        }
                    }
                    Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val photoFile = File(context.getExternalFilesDir(null), "temp_${System.currentTimeMillis()}.jpg")
                val photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                tempPhotoUri = photoUri
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val savedImagePath = saveImageToAppStorage(context, selectedUri)
            if (savedImagePath != null) {
                profileImageUrl = savedImagePath
                currentUser?.let { user ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.updateUserProfile(
                            userId = user.uid,
                            name = name,
                            profileImageUrl = savedImagePath
                        )
                    }
                }
                Toast.makeText(context, "Photo updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val badges = remember {
        listOf(
            Badge("first_login", "First Login", "Welcome to 24+1! You joined our community.", "🎉", true, Color(0xFF4CAF50)),
            Badge("week_streak", "Week Streak", "Maintained activity for 7 consecutive days.", "🔥", true, Color(0xFFFF5722)),
            Badge("month_active", "Monthly Active", "Stay active for 30 days to unlock this badge.", "⭐", false, Color(0xFF9E9E9E)),
            Badge("achievement", "Achiever", "Complete 10 objectives to earn this badge.", "🏆", false, Color(0xFF9E9E9E)),
        )
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            email = user.email ?: ""
            name = user.displayName ?: ""

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userEntity = repository.getUser(user.uid)
                    userEntity?.profileImageUrl?.let { savedImageUrl ->
                        if (File(savedImageUrl).exists()) {
                            profileImageUrl = savedImageUrl
                        }
                    }

                    if (profileImageUrl == null) {
                        profileImageUrl = user.photoUrl?.toString()
                    }
                } catch (e: Exception) {
                    profileImageUrl = user.photoUrl?.toString()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { showImagePicker = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, onPrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl != null) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(94.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(50.dp),
                            tint = onPrimaryLight
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primaryContainerLightMediumContrast)
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(18.dp),
                        tint = onPrimaryLightMediumContrast
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Name",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your name") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "Email",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = email,
                onValueChange = { },
                placeholder = { Text("Your email") },
                singleLine = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Achievements",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 20.sp,
                    color = onPrimaryLight,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(badges) { badge ->
                    BadgeCard(
                        badge = badge,
                        onClick = { selectedBadge = badge }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                            if (profileImageUrl != null && profileImageUrl!!.isNotEmpty()) {
                                try {
                                    photoUri = Uri.parse(profileImageUrl)
                                } catch (e: Exception) {
                                }
                            }
                        }

                        currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                currentUser.let { user ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.updateUserProfile(
                                            userId = user.uid,
                                            name = name,
                                            profileImageUrl = profileImageUrl
                                        )
                                    }
                                }

                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                    Text(text = "Save Profile")
                }
            }

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightHighContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text(text = "Logout")
            }
        }
    }

    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Choose Photo") },
            text = { Text("How would you like to add your profile photo?") },
            confirmButton = {
                Column {
                    Row {
                        TextButton(
                            onClick = {
                                showImagePicker = false
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                        try {
                                            val photoFile = File(context.getExternalFilesDir(null), "temp_${System.currentTimeMillis()}.jpg")
                                            val photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                            tempPhotoUri = photoUri
                                            cameraLauncher.launch(photoUri)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                showImagePicker = false
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Icon(Icons.Default.AccountBox, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showImagePicker = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
    }

    selectedBadge?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.icon,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = badge.name,
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) badge.color else Color.Gray
                            )
                        )
                        Text(
                            text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) badge.color.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = badge.description,
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (!badge.isUnlocked) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.DarkGray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Keep using the app to unlock this badge!",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = badge.color.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Congratulations! You've earned this badge.",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = badge.color.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                badge.color.copy(alpha = 0.15f)
            } else {
                Color.Gray.copy(alpha = 0.05f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 6.dp else 2.dp
        ),
        border = if (badge.isUnlocked) {
            BorderStroke(1.dp, badge.color.copy(alpha = 0.3f))
        } else {
            BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            badge.color.copy(alpha = 0.2f)
                        } else {
                            Color.Gray.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badge.isUnlocked) {
                    Text(
                        text = badge.icon,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                } else {
                    Text(
                        text = "🔒",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.name,
                fontSize = 9.sp,
                fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                color = if (badge.isUnlocked) badge.color else Color.Gray,
                style = TextStyle(textAlign = TextAlign.Center),
                maxLines = 2
            )

            if (!badge.isUnlocked) {
                Text(
                    text = "LOCKED",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray.copy(alpha = 0.6f),
                    style = TextStyle(textAlign = TextAlign.Center),
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "UNLOCKED",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray.copy(alpha = 0.8f),
                    style = TextStyle(textAlign = TextAlign.Center),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun saveImageToAppStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val permanentFile = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(permanentFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        permanentFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}