package com.example.a24.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a24.ui.theme.secondaryLight

@Composable
fun ProductivityStreakSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(secondaryLight)
            .padding(16.dp)
    ) {
        // Titolo della sezione
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )


            Text(
                text = "PRODUCTIVITY STREAK",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        // Progresso di oggi
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY'S PROGRESS:",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "70%",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Barra di progresso
        LinearProgressIndicator(
            progress = 0.7f,
            color = Color.Black,
            backgroundColor = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        // Bottone "+" per aggiungere task
        FloatingActionButton(
            onClick = { /* Azione da definire */ },
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.End)
                .padding(end = 16.dp, top = 16.dp),
            containerColor = Color.Gray
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                tint = Color.White
            )
        }
    }
}