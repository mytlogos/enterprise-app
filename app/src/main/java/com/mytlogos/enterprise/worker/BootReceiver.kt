package com.mytlogos.enterprise.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            BootReceiver.startWorker(context);
        }
    }

    public static void startWorker(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest periodicSynchronize = new PeriodicWorkRequest
                .Builder(SynchronizeWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SynchronizeWorker.SYNCHRONIZE_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicSynchronize
        );

        PeriodicWorkRequest periodicCheckSaved = new PeriodicWorkRequest
                .Builder(CheckSavedWorker.class, 1, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CheckSavedWorker.CHECK_SAVED_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicCheckSaved
        );
    }
}
