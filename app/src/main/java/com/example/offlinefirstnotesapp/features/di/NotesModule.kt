package com.example.offlinefirstnotesapp.features.di

import com.example.offlinefirstnotesapp.features.notes.data.remote.api.NotesApi
import com.example.offlinefirstnotesapp.features.notes.data.repository.NotesLocalRepositoryImpl
import com.example.offlinefirstnotesapp.features.notes.data.repository.NotesRemoteRepositoryImpl
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesLocalRepository
import com.example.offlinefirstnotesapp.features.notes.domain.repository.NotesRemoteRepository
import com.example.offlinefirstnotesapp.features.notes.domain.usecase.NotesUseCase
import com.example.offlinefirstnotesapp.features.notes.ui.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val notesModule = module {
    single<NotesApi> { get<Retrofit>().create(NotesApi::class.java) }
    single<NotesLocalRepository> { NotesLocalRepositoryImpl(get()) }
    single<NotesRemoteRepository> { NotesRemoteRepositoryImpl(get()) }
    single { NotesUseCase(get(), get(), get()) }
    viewModel { NotesViewModel(get(), get(), get()) }
}