package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.ProductivityStreakSection
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.theme.secondaryLight
import com.example.a24.ui.theme.tertiaryLight

@Composable
fun HomeScreen(){
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppBar()
            Spacer(modifier = Modifier.height(16.dp))

            // Sezione "Hai qualcosa da fare?"
            SectionHeader(text = "DO YOU HAVE SOMETHING TO DO?")

            // Task di oggi
            TaskSection(title = "TODAY") {
                TaskItem(isCompleted = true)
                TaskItem(isCompleted = true)
            }

        }
    }
}

@Composable
fun TaskSection(title: String, tasks: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(secondaryLight)
    ) {
        // Titolo della sezione
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        // Lista dei task
        tasks()
    }
}

@Composable
fun TaskItem(isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(primaryLight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = isCompleted,
            onCheckedChange = {},
            colors = CheckboxDefaults.colors(
                checkedColor = tertiaryLight,
                uncheckedColor = Color.Gray
            )
        )

        // Testo del task (può essere vuoto o personalizzato)
        Text(
            text = "",
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Sezione Productivity Streak
    ProductivityStreakSection()
}


