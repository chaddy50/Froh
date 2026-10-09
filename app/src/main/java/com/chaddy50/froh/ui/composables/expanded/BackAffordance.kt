package com.chaddy50.froh.ui.composables.expanded

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.navigation.AppNavigator

@Composable
fun BackAffordance(
    label: String,
    appNavigator: AppNavigator,
) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .clickable { appNavigator.pop() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
