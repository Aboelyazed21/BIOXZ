package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EngZezoSignature(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "© 2026 ENG ZEZO. ALL RIGHTS RESERVED.",
            fontSize = 10.sp,
            color = Color(0xFF64748B), // Slate 500
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "BIOXZ Bioinformatics Intelligence Platform v2.4.1",
            fontSize = 9.sp,
            color = Color(0xFF94A3B8), // Slate 400
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}
