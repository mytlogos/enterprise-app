package com.mytlogos.enterprise.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            startWorker(context)
        }
    }

    companion object {
        @JvmStatic
        fun startWorker(context: Context?) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodicSynchronize = PeriodicWorkRequest.Builder(
                SynchronizeWorker::class.java,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context!!).enqueueUniquePeriodicWork(
                SynchronizeWorker.SYNCHRONIZE_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicSynchronize
            )
            val periodicCheckSaved = PeriodicWorkRequest.Builder(
                CheckSavedWorker::class.java, 1, TimeUnit.HOURS
            )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CheckSavedWorker.CHECK_SAVED_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicCheckSaved
            )
        }
    }
}