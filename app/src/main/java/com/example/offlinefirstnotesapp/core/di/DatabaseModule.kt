package com.example.offlinefirstnotesapp.core.di

import com.example.offlinefirstnotesapp.core.database.OfflineFirstNotesAppDB
import com.example.offlinefirstnotesapp.core.utils.NetworkConnectivityObserver
import com.example.offlinefirstnotesapp.features.notes.data.worker.SyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { OfflineFirstNotesAppDB.getInstance(androidContext()) }
    single { get<OfflineFirstNotesAppDB>().notesDao() }
    single { NetworkConnectivityObserver(androidContext()) }
    single { SyncScheduler(androidContext()) }
}
