package com.example.a24.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}