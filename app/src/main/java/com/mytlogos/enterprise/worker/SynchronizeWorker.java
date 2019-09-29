package com.mytlogos.enterprise.worker;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SynchronizeWorker extends Worker {
    static final String SYNCHRONIZE_WORKER = "SYNCHRONIZE_WORKER";
    // TODO: 08.08.2019 use this for sdk >= 28
    private static final String CHANNEL_ID = "SYNC_CHANNEL";
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private final int syncNotificationId = 0x200;
    private final Consumer<Integer> progressListener = progress -> {
        if (this.builder == null || this.notificationManager == null) {
            return;
        }
        int totalWork = RepositoryImpl.getInstance().getLoadWorkerTotalWork();
        builder.setProgress(totalWork, progress, totalWork < 0);

        notificationManager.notify(syncNotificationId, builder.build());
    };
    private final Consumer<Integer> totalWorkListener = totalWork -> {
        if (this.builder == null || this.notificationManager == null) {
            return;
        }

        int progress = RepositoryImpl.getInstance().getLoadWorkerProgress();
        builder.setProgress(totalWork, progress, totalWork < 0);

        notificationManager.notify(syncNotificationId, builder.build());
    };
    private static volatile UUID uuid;

    public SynchronizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        uuid = this.getId();
    }

    public static void enqueueOneTime(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                .Builder(SynchronizeWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context)
                .beginUniqueWork(SYNCHRONIZE_WORKER, ExistingWorkPolicy.REPLACE, workRequest)
                .enqueue();
    }

    public static void stopWorker(Application application) {
        if (uuid == null) {
            return;
        }
        WorkManager.getInstance(application).cancelWorkById(uuid);
    }

    public static boolean isRunning(Application application) {
        if (uuid == null) {
            return false;
        }
        ListenableFuture<WorkInfo> infoFuture = WorkManager.getInstance(application).getWorkInfoById(uuid);
        try {
            return infoFuture.get().getState() == WorkInfo.State.RUNNING;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
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
                cleanUp();
                return Result.retry();
            }
            notificationManager = NotificationManagerCompat.from(this.getApplicationContext());
            builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID);
            builder
                    .setContentTitle("Synchronizing...")
                    .setContentText("Updating User")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            repository.addTotalWorkListener(this.totalWorkListener);
            repository.addProgressListener(this.progressListener);

            notificationManager.notify(syncNotificationId, builder.build());
            repository.syncUser();

            if (this.isStopped()) {
                return this.stopSynchronize();
            }

            builder.setContentText("Updating Content data");
            notificationManager.notify(syncNotificationId, builder.build());
            repository.loadInvalidated();

            if (this.isStopped()) {
                return this.stopSynchronize();
            }

            builder.setContentText("Loading new Media");
            notificationManager.notify(syncNotificationId, builder.build());
            repository.loadAllMedia();

            builder
                    .setContentTitle("Synchronization complete")
                    .setProgress(1, 1, false)
                    .setContentText(null);

            notificationManager.notify(syncNotificationId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();

            builder
                    .setContentTitle("Synchronization failed")
                    .setProgress(0, 0, false)
                    .setContentText(null);

            notificationManager.notify(syncNotificationId, builder.build());
            cleanUp();
            return Result.failure();
        }
        cleanUp();
        return Result.success();
    }

    private Result stopSynchronize() {
        notificationManager.notify(
                syncNotificationId,
                builder
                        .setContentTitle("Synchronization stopped")
                        .setContentText(null)
                        .build()
        );
        cleanUp();
        return Result.failure();
    }

    private void cleanUp() {
        Repository repository = RepositoryImpl.getInstance();
        repository.removeProgressListener(this.progressListener);
        repository.removeTotalWorkListener(this.totalWorkListener);
        repository.syncProgress();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (notificationManager != null) {
            notificationManager.cancel(syncNotificationId);
        }
        uuid = null;
    }

}
