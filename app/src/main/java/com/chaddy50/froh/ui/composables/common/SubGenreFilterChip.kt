package com.chaddy50.froh.ui.composables.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.chaddy50.froh.data.entity.Genre

private const val SUB_GENRE_FILTER_CONTENT_DESCRIPTION = "Filter by sub-genre"
private const val CLEAR_SUB_GENRE_FILTER_CONTENT_DESCRIPTION = "Clear sub-genre filter"

@Composable
fun SubGenreFilterChip(
    subGenres: List<Genre>,
    selectedSubGenreId: Long?,
    onSubGenreSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedSubGenre = subGenres.find { it.id == selectedSubGenreId }

    Box(modifier) {
        if (selectedSubGenre == null) {
            IconButton(onClick = { isDropdownExpanded = true }) {
                Icon(Icons.Filled.FilterList, contentDescription = SUB_GENRE_FILTER_CONTENT_DESCRIPTION)
            }
        } else {
            InputChip(
                selected = true,
                onClick = { isDropdownExpanded = true },
                label = { Text(selectedSubGenre.name) },
                leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = CLEAR_SUB_GENRE_FILTER_CONTENT_DESCRIPTION,
                        modifier = Modifier
                            .size(InputChipDefaults.AvatarSize)
                            .clickable { onSubGenreSelected(null) },
                    )
                },
                modifier = Modifier.semantics { contentDescription = SUB_GENRE_FILTER_CONTENT_DESCRIPTION },
            )
        }
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "All",
                        fontWeight = if (selectedSubGenreId == null) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onSubGenreSelected(null)
                    isDropdownExpanded = false
                },
            )
            subGenres.forEach { genre ->
                DropdownMenuItem(
                    text = {
                        Text(
                            genre.name,
                            fontWeight = if (selectedSubGenreId == genre.id) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSubGenreSelected(genre.id)
                        isDropdownExpanded = false
                    },
                )
            }
        }
    }
}
