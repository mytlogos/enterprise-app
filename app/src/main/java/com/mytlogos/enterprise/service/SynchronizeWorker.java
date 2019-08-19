package com.mytlogos.enterprise.service;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;

public class SynchronizeWorker extends Worker {
    static final String SYNCHRONIZE_WORKER = "SYNCHRONIZE_WORKER";
    // TODO: 08.08.2019 use this for sdk >= 28
    private static final String CHANNEL_ID = "SYNC_CHANNEL";
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private final int syncNotificationId = 0x200;


    public SynchronizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueueOneTime() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                .Builder(SynchronizeWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance()
                .beginUniqueWork(SYNCHRONIZE_WORKER, ExistingWorkPolicy.REPLACE, workRequest)
                .enqueue();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!(this.getApplicationContext() instanceof Application)) {
            System.out.println("Context not instance of Application");
            return Result.failure();
        }
        Application application = (Application) this.getApplicationContext();
        try {
            Repository repository = RepositoryImpl.getInstance(application);

            if (!repository.isClientAuthenticated()) {
                return Result.retry();
            }
            notificationManager = NotificationManagerCompat.from(this.getApplicationContext());
            builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID);
            builder
                    .setContentTitle("Synchronizing...")
                    .setContentText("Updating User")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(syncNotificationId, builder.build());
            repository.syncUser();

            builder.setContentText("Updating Content data");
            notificationManager.notify(syncNotificationId, builder.build());
            repository.loadInvalidated();

            notificationManager.notify(syncNotificationId, builder.build());

            builder.setContentText("Loading new Media");
            notificationManager.notify(syncNotificationId, builder.build());
            repository.loadAllMedia();

            builder
                    .setContentTitle("Synchronization complete")
                    .setContentText(null);

            notificationManager.notify(syncNotificationId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();

            builder
                    .setContentTitle("Synchronization failed")
                    .setContentText(null);

            notificationManager.notify(syncNotificationId, builder.build());
            return Result.failure();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notificationManager.cancel(syncNotificationId);
        return Result.success();
    }
}
