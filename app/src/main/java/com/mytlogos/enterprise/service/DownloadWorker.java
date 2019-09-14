package com.mytlogos.enterprise.service;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.model.FailedEpisode;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.NotificationItem;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.model.SimpleMedium;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.tools.ContentTool;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.NotEnoughSpaceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DownloadWorker extends MonitorableWorker {
    private static final String UNIQUE = "DOWNLOAD_WORKER";
    // TODO: 08.08.2019 use this for sdk >= 28
    private static final String CHANNEL_ID = "DOWNLOAD_CHANNEL";

    private static final int maxEpisodeLimit = 50;
    private static final int maxPackageSize = 1;
    private static final String mediumId = "mediumId";
    private static final String episodeIds = "episodeIds";
    private static Set<UUID> uuids = Collections.synchronizedSet(new HashSet<>());

    private final int downloadNotificationId = 0x100;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private Set<ContentTool> contentTools;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        DownloadWorker.uuids.add(this.getId());
    }

    @Override
    String getWorkerName() {
        return "DownloadWorker";
    }

    public static void enqueueDownloadTask(Context context) {
        OneTimeWorkRequest oneTimeWorkRequest = getWorkRequest(null);
        WorkManager.getInstance(context)
                .beginUniqueWork(UNIQUE, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                .then(CheckSavedWorker.getWorkRequest())
                .enqueue();
    }

    private static OneTimeWorkRequest getWorkRequest(Data data) {
        if (data == null) {
            data = Data.EMPTY;
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        return new OneTimeWorkRequest
                .Builder(DownloadWorker.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();
    }

    public static void watchDatabase(Application application, LifecycleOwner owner) {
        Repository repository = RepositoryImpl.getInstance(application);

        LiveData<Boolean> doDownload = repository.onDownloadable();
        doDownload.observe(owner, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                enqueueDownloadTask(application);
            }
        });
        enqueueDownloadTask(application);
    }

    public static void stopWorker(Application application) {
        WorkManager.getInstance(application).cancelUniqueWork(UNIQUE);
    }

    public static boolean isRunning(Application application) {
        for (UUID uuid : DownloadWorker.uuids) {
            ListenableFuture<WorkInfo> infoFuture = WorkManager.getInstance(application).getWorkInfoById(uuid);
            try {
                WorkInfo info = infoFuture.get();
                if (info == null) {
                    continue;
                }
                if (info.getState() == WorkInfo.State.RUNNING) {
                    return true;
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void enqueueDownloadTask(Context context, int mediumId, Collection<Integer> episodeIds) {
        Data data = new Data.Builder()
                .putInt(DownloadWorker.mediumId, mediumId)
                .putIntArray(DownloadWorker.episodeIds, episodeIds.stream().mapToInt(Integer::intValue).toArray())
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = getWorkRequest(data);
        String uniqueWorkName = UNIQUE + "-" + mediumId;
        WorkManager.getInstance(context)
                .beginUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                .then(CheckSavedWorker.getWorkRequest())
                .enqueue();

    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        if (!(this.getApplicationContext() instanceof Application)) {
            System.out.println("Context not instance of Application");
            return Result.failure();
        }
        Application application = (Application) this.getApplicationContext();

        if (SynchronizeWorker.isRunning(application)) {
            return Result.retry();
        }

        notificationManager = NotificationManagerCompat.from(this.getApplicationContext());
        builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID);
        builder
                .setContentTitle("Download in Progress...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // todo read limit from settings
        try {
            synchronized (UNIQUE) {
                Repository repository = RepositoryImpl.getInstance(application);

                this.contentTools = FileTools.getSupportedContentTools(application);

                for (ContentTool tool : this.contentTools) {
                    tool.mergeIfNecessary();
                }

                if (!repository.isClientAuthenticated()) {
                    return Result.retry();
                }
                if (!repository.isClientOnline()) {
                    notificationManager.notify(
                            downloadNotificationId,
                            builder.setContentTitle("Server not in reach").build()
                    );
                    cleanUp();
                    return Result.failure();
                }
                if (!FileTools.writable(application)) {
                    notificationManager.notify(
                            downloadNotificationId,
                            builder.setContentTitle("Not enough free space").build()
                    );
                    cleanUp();
                    return Result.failure();
                }
                if (this.getInputData().equals(Data.EMPTY)) {
                    this.download(repository);
                } else {
                    this.downloadData(repository);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            notificationManager.notify(
                    downloadNotificationId,
                    builder.setContentTitle("Download failed").setContentText(null).build()
            );
            cleanUp();
            return Result.failure();
        }
        cleanUp();
        return Result.success();
    }

    private void download(Repository repository) {
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

        Set<MediumDownload> mediumDownloads = new HashSet<>();
        int downloadCount = 0;

        for (Integer mediumId : toDownloadMedia) {
            // TODO: 26.07.2019 check whether the episodes are downloaded in the correct order
            //  so that there aren't gaps in the episodes one can't read
            if (repository.countSavedUnreadEpisodes(mediumId) >= maxEpisodeLimit) {
                continue;
            }
            SimpleMedium medium = repository.getSimpleMedium(mediumId);

            List<Integer> episodeIds = repository.getDownloadableEpisodes(mediumId);
            Set<Integer> uniqueEpisodes = new HashSet<>(episodeIds);

            if (uniqueEpisodes.isEmpty()) {
                continue;
            }
            List<FailedEpisode> failedEpisodes = repository.getFailedEpisodes(uniqueEpisodes);

            for (FailedEpisode failedEpisode : failedEpisodes) {
                // if it failed 3 times or more, don't try anymore for now
                if (failedEpisode.getFailCount() < 3) {
                    continue;
                }
                uniqueEpisodes.remove(failedEpisode.getEpisodeId());
            }

            if (uniqueEpisodes.isEmpty()) {
                continue;
            }

            mediumDownloads.add(new MediumDownload(
                    uniqueEpisodes,
                    mediumId,
                    medium.getMedium(),
                    medium.getTitle()
            ));
            downloadCount += uniqueEpisodes.size();
        }
        if (!mediumDownloads.isEmpty()) {
            this.downloadEpisodes(mediumDownloads, repository, downloadCount);
        }
    }

    private void downloadData(Repository repository) {
        Data data = this.getInputData();
        int mediumId = data.getInt(DownloadWorker.mediumId, 0);
        int[] episodeIds = data.getIntArray(DownloadWorker.episodeIds);

        if (mediumId == 0 || episodeIds == null || episodeIds.length == 0) {
            return;
        }
        HashSet<Integer> episodes = new HashSet<>();
        for (int episodeId : episodeIds) {
            episodes.add(episodeId);
        }
        SimpleMedium medium = repository.getSimpleMedium(mediumId);
        MediumDownload download = new MediumDownload(episodes, mediumId, medium.getMedium(), medium.getTitle());
        this.downloadEpisodes(Collections.singleton(download), repository, episodeIds.length);
    }

    /**
     * Download episodes for each medium id,
     * up to an episode limit initialized in {@link #maxEpisodeLimit}.
     */
    private void downloadEpisodes(Set<MediumDownload> episodeIds, Repository repository, int downloadCount) {
        builder
                .setContentTitle("Download in Progress [0/" + downloadCount + "]")
                .setProgress(downloadCount, 0, true);
        notificationManager.notify(downloadNotificationId, builder.build());

        Collection<DownloadPackage> episodePackages = getDownloadPackages(episodeIds, repository);

        int successFull = 0;
        int notSuccessFull = 0;

        for (DownloadPackage episodePackage : episodePackages) {
            if (this.isStopped()) {
                this.stopDownload();
                return;
            }

            ContentTool contentTool = null;
            for (ContentTool tool : this.contentTools) {
                if (MediumType.is(tool.getMedium(), episodePackage.mediumType) && tool.isSupported()) {
                    contentTool = tool;
                    break;
                }
            }
            if (contentTool == null) {
                notSuccessFull += episodePackage.episodeIds.size();

                updateProgress(downloadCount, successFull, notSuccessFull);
                continue;
            }
            try {
                List<ClientDownloadedEpisode> downloadedEpisodes = repository.downloadEpisodes(episodePackage.episodeIds);

                List<Integer> currentlySavedEpisodes = new ArrayList<>();

                if (downloadedEpisodes == null) {
                    notSuccessFull = onFailed(
                            downloadCount,
                            successFull,
                            notSuccessFull,
                            repository,
                            episodePackage,
                            false);
                    continue;
                }
                List<ClientDownloadedEpisode> contentEpisodes = new ArrayList<>();

                for (ClientDownloadedEpisode downloadedEpisode : downloadedEpisodes) {
                    int episodeId = downloadedEpisode.getEpisodeId();
                    SimpleEpisode episode = repository.getSimpleEpisode(episodeId);

                    if (downloadedEpisode.getContent().length > 0) {
                        successFull++;
                        currentlySavedEpisodes.add(episodeId);
                        contentEpisodes.add(downloadedEpisode);
                        repository.addNotification(NotificationItem.createNow(
                                String.format("Episode %s of %s saved", episode.getFormattedTitle(), episodePackage.mediumTitle),
                                ""
                        ));
                    } else {
                        notSuccessFull++;
                        repository.updateFailedDownloads(episodeId);
                        repository.addNotification(NotificationItem.createNow(
                                String.format("Could not save Episode %s of %s", episode.getFormattedTitle(), episodePackage.mediumTitle),
                                ""
                        ));
                    }
                }
                contentTool.saveContent(contentEpisodes, episodePackage.mediumId);
                repository.updateSaved(currentlySavedEpisodes, true);

                updateProgress(downloadCount, successFull, notSuccessFull);
            } catch (NotEnoughSpaceException e) {
                notSuccessFull = onFailed(
                        downloadCount,
                        successFull,
                        notSuccessFull,
                        repository,
                        episodePackage,
                        true
                );
            } catch (IOException e) {
                e.printStackTrace();
                notSuccessFull = onFailed(
                        downloadCount,
                        successFull,
                        notSuccessFull,
                        repository,
                        episodePackage,
                        false);
            }
        }
    }

    private void stopDownload() {
        notificationManager.notify(
                downloadNotificationId,
                builder
                        .setContentTitle("Download stopped")
                        .setContentText(null)
                        .build()
        );
    }

    private void cleanUp() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notificationManager.cancel(downloadNotificationId);
        DownloadWorker.uuids.remove(this.getId());
    }

    private int onFailed(int downloadCount, int successFull, int notSuccessFull, Repository repository, DownloadPackage downloadPackage, boolean notEnoughSpace) {
        notSuccessFull += downloadPackage.episodeIds.size();

        for (Integer episodeId : downloadPackage.episodeIds) {
            repository.updateFailedDownloads(episodeId);

            SimpleEpisode episode = repository.getSimpleEpisode(episodeId);
            String format = notEnoughSpace ? "Not enough Space for Episode %s of %s" : "Could not save Episode %s of %s";

            repository.addNotification(NotificationItem.createNow(
                    String.format(format, episode.getFormattedTitle(), downloadPackage.mediumTitle),
                    ""
            ));
        }

        updateProgress(downloadCount, successFull, notSuccessFull);
        return notSuccessFull;
    }

    private void updateProgress(int downloadCount, int successFull, int notSuccessFull) {
        int progress = successFull + notSuccessFull;
        builder.setContentTitle(String.format("Download in Progress [%s/%s]", progress, downloadCount));
        builder.setContentText(String.format("Failed: %s", notSuccessFull));
        builder.setProgress(downloadCount, progress, false);
        notificationManager.notify(downloadNotificationId, builder.build());
    }

    private static class MediumDownload {
        private final Set<Integer> toDownloadEpisodes;
        private final int id;
        private final int mediumType;
        private final String title;

        private MediumDownload(Set<Integer> toDownloadEpisodes, int id, int mediumType, String title) {
            this.toDownloadEpisodes = toDownloadEpisodes;
            this.id = id;
            this.mediumType = mediumType;
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediumDownload that = (MediumDownload) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class DownloadPackage {
        private final Set<Integer> episodeIds = new HashSet<>();
        private final int mediumId;
        private final int mediumType;
        private final String mediumTitle;

        private DownloadPackage(int mediumId, int mediumType, String mediumTitle) {
            this.mediumId = mediumId;
            this.mediumType = mediumType;
            this.mediumTitle = mediumTitle;
        }
    }


    private Collection<DownloadPackage> getDownloadPackages(Set<MediumDownload> episodeIds, Repository repository) {
        List<Integer> savedEpisodes = repository.getSavedEpisodes();

        Set<Integer> savedIds = new HashSet<>(savedEpisodes);

        Collection<DownloadPackage> episodePackages = new ArrayList<>();

        for (MediumDownload mediumDownload : episodeIds) {
            DownloadPackage downloadPackage = new DownloadPackage(
                    mediumDownload.id,
                    mediumDownload.mediumType,
                    mediumDownload.title
            );

            for (Integer episodeId : mediumDownload.toDownloadEpisodes) {

                if (savedIds.contains(episodeId)) {
                    continue;
                }

                if (downloadPackage.episodeIds.size() == maxPackageSize) {
                    episodePackages.add(downloadPackage);
                    downloadPackage = new DownloadPackage(
                            mediumDownload.id,
                            mediumDownload.mediumType,
                            mediumDownload.title
                    );
                }

                downloadPackage.episodeIds.add(episodeId);
            }
            if (!downloadPackage.episodeIds.isEmpty()) {
                episodePackages.add(downloadPackage);
            }
        }
        return episodePackages;
    }
}
