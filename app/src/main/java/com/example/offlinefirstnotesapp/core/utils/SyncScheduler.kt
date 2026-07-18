package com.example.offlinefirstnotesapp.core.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.offlinefirstnotesapp.features.notes.data.worker.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Schedules background synchronization tasks using WorkManager.
 */
class SyncScheduler(private val context: Context) {

    /**
     * Schedules a periodic fallback sync to ensure eventual consistency.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Schedules an immediate one-time sync for local changes.
     */
    fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "OneTimeSync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
