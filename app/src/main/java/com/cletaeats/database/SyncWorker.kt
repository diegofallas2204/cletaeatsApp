package com.cletaeats.database

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cletaeats.utils.currentConnectivityState
import com.cletaeats.utils.ConnectionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (applicationContext.currentConnectivityState !is ConnectionState.Available) {
            return Result.retry()
        }
        SyncManager.sincronizar()
        withTimeoutOrNull(30_000L) {
            SyncManager.syncCompleted.first()
        }
        return Result.success()
    }
}
