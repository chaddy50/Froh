package com.chaddy50.froh.ui.composables.compact

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.chaddy50.froh.data.entity.Genre

@Composable
fun SubGenreFilterButton(
    subGenres: List<Genre>,
    selectedSubGenreId: Long?,
    onSubGenreSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = "Filter by sub-genre"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "All",
                        fontWeight = if (selectedSubGenreId == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onSubGenreSelected(null)
                    expanded = false
                }
            )
            subGenres.forEach { genre ->
                DropdownMenuItem(
                    text = {
                        Text(
                            genre.name,
                            fontWeight = if (selectedSubGenreId == genre.id) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSubGenreSelected(genre.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
