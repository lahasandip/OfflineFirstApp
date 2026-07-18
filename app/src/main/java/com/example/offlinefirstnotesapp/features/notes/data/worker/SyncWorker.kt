package  com.example.offlinefirstnotesapp.features.notes.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import  com.example.offlinefirstnotesapp.features.notes.domain.usecase.NotesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val notesUseCase: NotesUseCase by inject()

    override suspend fun doWork(): Result {
        return try {
            notesUseCase.sync()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
