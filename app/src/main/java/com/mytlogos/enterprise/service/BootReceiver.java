package com.mytlogos.enterprise.service;

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
            BootReceiver.startWorker();
        }
    }

    public static void startWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
                .Builder(SynchronizeWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueueUniquePeriodicWork(
                SynchronizeWorker.SYNCHRONIZE_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
        );
    }
}
