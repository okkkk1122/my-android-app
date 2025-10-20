package com.gymway.auth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFF6200EE) else Color.LightGray,
        contentColor = if (selected) Color.White else Color.Black,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .padding(4.dp)
            .clickable { onSelected() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
