package com.mytlogos.enterprise.service;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.tools.ContentTool;
import com.mytlogos.enterprise.tools.FileTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckSavedWorker extends Worker {
    static final String CHECK_SAVED_WORKER = "CHECK_SAVED_WORKER";
    // TODO: 08.08.2019 use this for sdk >= 28
    static final String CHANNEL_ID = "CHECK_SAVED_WORKER";
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private final int checkLocalNotificationId = 0x300;
    private int correctedSaveState = 0;
    private int clearedLooseEpisodes = 0;

    public CheckSavedWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void checkLocal(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        WorkManager.getInstance(context)
                .beginWith(CheckSavedWorker.getWorkRequest())
                .then(new OneTimeWorkRequest.Builder(DownloadWorker.class)
                        .setConstraints(constraints).build())
                .enqueue();
    }

    static OneTimeWorkRequest getWorkRequest() {
        return new OneTimeWorkRequest.Builder(CheckSavedWorker.class).build();
    }

    @NonNull
    @Override
    @SuppressLint("UseSparseArrays")
    public Result doWork() {
        Application application = (Application) this.getApplicationContext();

        if (SynchronizeWorker.isRunning(application) || DownloadWorker.isRunning(application)) {
            return Result.retry();
        }

        notificationManager = NotificationManagerCompat.from(this.getApplicationContext());
        builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID);
        builder
                .setContentTitle("Checking Local Content Integrity")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(checkLocalNotificationId, builder.build());

        Set<ContentTool> tools = FileTools.getSupportedContentTools(application);
        Repository repository = RepositoryImpl.getInstance(application);

        Map<Integer, Set<Integer>> mediumSavedEpisodes = new HashMap<>();

        for (ContentTool tool : tools) {
            this.putItemContainer(tool, mediumSavedEpisodes, true, repository);
            this.putItemContainer(tool, mediumSavedEpisodes, false, repository);
        }

        Map<Integer, Map<Integer, Set<Integer>>> typeMediumSavedEpisodes = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : mediumSavedEpisodes.entrySet()) {
            int mediumType = repository.getMediumType(entry.getKey());
            typeMediumSavedEpisodes
                    .computeIfAbsent(mediumType, integer -> new HashMap<>())
                    .put(entry.getKey(), entry.getValue());
        }
        int mediaToCheck = 0;

        for (Map<Integer, Set<Integer>> map : typeMediumSavedEpisodes.values()) {
            mediaToCheck += map.size();
        }

        builder.setContentTitle(String.format("Checking Local Content Integrity [0/%s]", mediaToCheck));
        notificationManager.notify(checkLocalNotificationId, builder.build());

        int checkedCount = 0;

        for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : typeMediumSavedEpisodes.entrySet()) {
            Integer mediumType = entry.getKey();
            ContentTool tool = FileTools.getContentTool(mediumType, application);
            checkLocalContentFiles(tool, repository, entry.getValue());

            checkedCount++;
            builder.setContentTitle(String.format(
                    "Checking Local Content Integrity [%s/%s]",
                    checkedCount,
                    mediaToCheck
            ));
            notificationManager.notify(checkLocalNotificationId, builder.build());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notificationManager.cancel(checkLocalNotificationId);
        return Result.success();
    }

    private void checkLocalContentFiles(ContentTool tool, Repository repository, Map<Integer, Set<Integer>> mediumSavedEpisodes) {
        for (Map.Entry<Integer, Set<Integer>> entry : mediumSavedEpisodes.entrySet()) {
            List<Integer> savedEpisodes = repository.getSavedEpisodes(entry.getKey());

            Set<Integer> unSavedIds = new HashSet<>(savedEpisodes);
            unSavedIds.removeAll(entry.getValue());

            Set<Integer> looseIds = entry.getValue();
            looseIds.removeAll(savedEpisodes);

            if (!unSavedIds.isEmpty()) {
                repository.updateSaved(unSavedIds, false);
                this.correctedSaveState += unSavedIds.size();
                updateNotificationContentText();
            }

            if (looseIds.isEmpty()) {
                continue;
            }
            try {
                tool.removeMediaEpisodes(entry.getKey(), looseIds);
                this.clearedLooseEpisodes += looseIds.size();
                updateNotificationContentText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateNotificationContentText() {
        this.builder.setContentText(String.format(
                "Corrected Save State: %s, Cleared Loose Episodes: %s",
                this.correctedSaveState,
                this.clearedLooseEpisodes
        ));
        this.notificationManager.notify(this.checkLocalNotificationId, this.builder.build());
    }

    private void putItemContainer(ContentTool bookTool, Map<Integer, Set<Integer>> mediumSavedEpisodes, boolean externalSpace, Repository repository) {
        for (Map.Entry<Integer, File> entry : bookTool.getItemContainers(externalSpace).entrySet()) {
            Map<Integer, String> episodePaths = bookTool.getEpisodePaths(entry.getValue().getAbsolutePath());
            Set<Integer> episodeIds = new HashSet<>(episodePaths.keySet());

            mediumSavedEpisodes.merge(entry.getKey(), episodeIds, (integers, integers2) -> {
                integers.addAll(integers2);
                return integers;
            });
        }
        List<ToDownload> toDownloadList = repository.getToDownload();

        List<Integer> prohibitedMedia = new ArrayList<>();
        Set<Integer> toDownloadMedia = new HashSet<>();

        for (ToDownload toDownload : toDownloadList) {
            if (toDownload.getMediumId() != null) {
                if (toDownload.isProhibited()) {
                    prohibitedMedia.add(toDownload.getMediumId());
                } else {
                    toDownloadMedia.add(toDownload.getMediumId());
                }
            }

            if (toDownload.getExternalListId() != null) {
                toDownloadMedia.addAll(repository.getExternalListItems(toDownload.getExternalListId()));
            }

            if (toDownload.getListId() != null) {
                toDownloadMedia.addAll(repository.getListItems(toDownload.getListId()));
            }
        }

        toDownloadMedia.removeAll(prohibitedMedia);

        for (Integer mediumId : toDownloadMedia) {
            mediumSavedEpisodes.putIfAbsent(mediumId, new HashSet<>());
        }
    }
}
