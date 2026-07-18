package com.example.offlinefirstnotesapp.features.notes.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinefirstnotesapp.core.utils.SupabaseRealtimeManager
import com.example.offlinefirstnotesapp.core.utils.NetworkConnectivityObserver
import com.example.offlinefirstnotesapp.features.notes.domain.model.Note
import com.example.offlinefirstnotesapp.features.notes.domain.usecase.NotesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesViewModel(
    private val notesUseCase: NotesUseCase,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val realtimeManager: SupabaseRealtimeManager
): ViewModel() {

    private var _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        fetchNotes()
        sync()
        observeConnectivity()
        observeRealtime()
    }

    private fun observeRealtime() {
        realtimeManager.connect()
        viewModelScope.launch {
            realtimeManager.events.collect {
                Log.d("Sync", "Realtime change detected, syncing...")
                sync()
            }
        }
    }

    override fun onCleared() {
        realtimeManager.disconnect()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collectLatest { status ->
                if (status == NetworkConnectivityObserver.Status.Available) {
                    sync()
                }
            }
        }
    }

    private fun fetchNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            notesUseCase.getNotes().collectLatest {
                _notes.value = it
            }
        }
    }

    fun addNote(title: String, content: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            notesUseCase.addNote(title, content)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            notesUseCase.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            notesUseCase.deleteNote(note)
        }
    }

    fun sync(showLoading: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (showLoading) _isRefreshing.value = true
            notesUseCase.sync()
            if (showLoading) _isRefreshing.value = false
        }
    }
}