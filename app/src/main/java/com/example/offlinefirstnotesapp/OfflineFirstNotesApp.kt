package com.example.offlinefirstnotesapp

import android.app.Application
import com.example.offlinefirstnotesapp.core.di.databaseModule
import com.example.offlinefirstnotesapp.core.di.networkModule
import com.example.offlinefirstnotesapp.core.utils.SyncScheduler
import com.example.offlinefirstnotesapp.features.di.notesModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

/**
 * Base Application class for the OfflineFirstNotesApp.
 */
class OfflineFirstNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OfflineFirstNotesApp)
            modules(
                networkModule,
                databaseModule,
                notesModule
            )
        }
        
        // Initialize periodic background sync
        val syncScheduler: SyncScheduler = get()
        syncScheduler.schedulePeriodicSync()
    }
}
