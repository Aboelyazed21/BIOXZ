package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import com.example.data.NotebookEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotebookScreen(viewModel: BioViewModel) {
    val notes by viewModel.notes.collectAsState()
    var selectedNote by remember { mutableStateOf<NotebookEntry?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    // If notes update, keep selected note in sync
    LaunchedEffect(notes) {
        if (selectedNote != null) {
            selectedNote = notes.find { it.id == selectedNote!!.id }
        } else if (notes.isNotEmpty()) {
            selectedNote = notes.first()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Left Column: Note list (200.dp wide)
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESEARCH JOURNAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                IconButton(
                    onClick = {
                        isEditing = true
                        noteTitle = "New Journal Log Entry"
                        noteContent = "### Computational Experiment Log\n\n- Date: 2026-07-01\n- Method: sgRNA hybridization transfection\n- Result: "
                        selectedNote = null
                    },
                    modifier = Modifier.size(24.dp).testTag("add_note_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(notes) { entry ->
                    val isSelected = selectedNote?.id == entry.id
                    val dateString = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(entry.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                selectedNote = entry
                                isEditing = false
                            }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = entry.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (entry.content.length > 20) entry.content.take(20) + "..." else entry.content,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                                Text(
                                    text = dateString,
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Editor or viewer
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditing) {
                // Edit form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "EDITOR INSTRUCTIONS ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                if (noteTitle.isNotEmpty()) {
                                    if (selectedNote == null) {
                                        viewModel.addNotebookEntry(noteTitle, noteContent)
                                    } else {
                                        viewModel.updateNotebookEntry(selectedNote!!.id, noteTitle, noteContent)
                                    }
                                    isEditing = false
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("save_note_btn")
                        ) {
                            Text("Save Entry", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Log Title") },
                    modifier = Modifier.fillMaxWidth().testTag("note_title_field")
                )

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Research markdown notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("note_content_field"),
                    placeholder = { Text("Use markdown markers to format lists...") }
                )
            } else {
                // Viewer
                if (selectedNote == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No entry selected. Select a research journal card to display or tap '+' to log.", fontSize = 13.sp, color = Color.Gray)
                    }
                } else {
                    val entry = selectedNote!!
                    val dateString = SimpleDateFormat("EEEE, MMMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "JOURNAL LOG VIEWER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    isEditing = true
                                    noteTitle = entry.title
                                    noteContent = entry.content
                                },
                                modifier = Modifier.size(28.dp).testTag("edit_note_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteNotebookEntry(entry.id)
                                    selectedNote = null
                                },
                                modifier = Modifier.size(28.dp).testTag("delete_note_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(text = entry.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(text = dateString, fontSize = 11.sp, color = Color.Gray)

                    Divider(color = Color(0xFFE2E8F0))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(text = entry.content, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
