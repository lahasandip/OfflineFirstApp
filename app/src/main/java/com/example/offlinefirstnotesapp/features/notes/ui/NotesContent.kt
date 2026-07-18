package com.example.offlinefirstnotesapp.features.notes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinefirstnotesapp.core.theme.OfflineFirstNotesAppTheme
import com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    modifier: Modifier = Modifier,
    notesViewModel: NotesViewModel = koinViewModel()
) {
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val isRefreshing by notesViewModel.isRefreshing.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    var previousNotesCount by remember { mutableIntStateOf(notes.size) }

    // Scroll to top when a new note is added
    LaunchedEffect(notes.size) {
        if (notes.size > previousNotesCount) {
            listState.animateScrollToItem(0)
        }
        previousNotesCount = notes.size
    }
    
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    val clearInput = {
        noteTitle = ""
        noteContent = ""
        editingNote = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingNote != null) "Edit Note" else "New Note",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (editingNote != null) {
                        IconButton(onClick = clearInput) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.size(8.dp))
                
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.size(4.dp))
                
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Content") }
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (editingNote != null) {
                        TextButton(onClick = clearInput) {
                            Text("Cancel")
                        }
                    }
                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank()) {
                                editingNote?.let {
                                    notesViewModel.updateNote(it.copy(title = noteTitle, content = noteContent))
                                } ?: run {
                                    notesViewModel.addNote(noteTitle, noteContent)
                                }
                                clearInput()
                            }
                        }
                    ) {
                        Text(if (editingNote != null) "Update" else "Add Note")
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { notesViewModel.sync(showLoading = true) },
            modifier = Modifier.padding(paddingValues)
        ) {
            NotesList(
                notes = notes,
                listState = listState,
                onDelete = { notesViewModel.deleteNote(it) },
                onEdit = {
                    editingNote = it
                    noteTitle = it.title
                    noteContent = it.content
                }
            )
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onDelete: (Note) -> Unit = {},
    onEdit: (Note) -> Unit = {}
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                onDelete = { onDelete(note) },
                onEdit = { onEdit(note) },
                modifier = Modifier.animateItem()
            )
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, style = MaterialTheme.typography.titleLarge)
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
                Text(
                    text = if (note.isSynced) "Synced" else "Pending Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (note.isSynced) Color.Gray else MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
                Icon(
                    imageVector = if (note.isSynced) Icons.Default.CheckCircle else Icons.Default.Sync,
                    contentDescription = null,
                    tint = if (note.isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesContentPreview() {
    val sampleNotes = listOf(
        Note(id = "1", title = "Synced Note", isSynced = true),
        Note(id = "2", title = "Offline Note", isSynced = false),
    )
    OfflineFirstNotesAppTheme {
        NotesList(notes = sampleNotes)
    }
}
