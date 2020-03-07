package com.mytlogos.enterprise.worker;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

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
import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.ReloadPart;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.NotConnectedException;
import com.mytlogos.enterprise.background.api.model.ClientChangedEntities;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.preferences.UserPreferences;
import com.mytlogos.enterprise.tools.Utils;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import retrofit2.Response;

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

            notificationManager.notify(syncNotificationId, builder.build());
//            if (syncWithInvalidation(repository)) return this.stopSynchronize();
            syncWithTime(repository);

            notify(null, "Synchronization complete", 1);
        } catch (Exception e) {
            String contentText;
            if (e instanceof IOException) {
                if (e instanceof NotConnectedException) {
                    contentText = "Not connected with Server";
                } else {
                    contentText = "Error between App and Server";
                }
            } else {
                contentText = "Local Error";
            }
            e.printStackTrace();

            notify(contentText, "Synchronization failed", 0);
            cleanUp();
            return Result.failure();
        }
        cleanUp();
        return Result.success();
    }

    void notify(String contentText, String title, int finished) {
        this.notify(title, contentText, finished, finished);
    }

    void notify(String title, String contentText, int current, int total) {
        this.builder
                .setContentTitle(title)
                .setProgress(total, current, false)
                .setContentText(contentText);

        this.notificationManager.notify(this.syncNotificationId, this.builder.build());
    }

    boolean syncWithTime(Repository repository) throws IOException {
        Client client = repository.getClient(this);
        ClientModelPersister persister = repository.getPersister(this);

        DateTime lastSync = UserPreferences.getLastSync(this.getApplicationContext());
        syncChanged(lastSync, client, persister, repository);
        UserPreferences.setLastSync(this.getApplicationContext(), DateTime.now());
        syncDeleted(client, persister, repository);
        return false;
    }

    private <T> Map<Integer, T> mapStringToInt(Map<String, T> map) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, T> result = new HashMap<>();

        for (Map.Entry<String, T> entry : map.entrySet()) {
            result.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return result;
    }


    private void syncChanged(DateTime lastSync, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        Response<ClientChangedEntities> changedEntitiesResponse = client.getNew(lastSync);
        ClientChangedEntities changedEntities = Utils.checkAndGetBody(changedEntitiesResponse);

        // persist all new or updated entities, media to releases needs to be in this order
        persister.persistMedia(changedEntities.media);
        this.persistParts(changedEntities.parts, client, persister, repository);
        this.persistEpisodes(changedEntities.episodes, client, persister, repository);
        this.persistReleases(changedEntities.releases, client, persister, repository);
        persister.persistMediaLists(changedEntities.lists);
        persister.persistExternalUsers(changedEntities.extUser);
        this.persistExternalLists(changedEntities.extLists, client, persister, repository);
        persister.persistMediaInWait(changedEntities.mediaInWait);
        persister.persistNews(changedEntities.news);
    }

    private void persistParts(Collection<ClientPart> parts, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        Collection<Integer> missingIds = new HashSet<>();
        Collection<ClientPart> loadingParts = new HashSet<>();

        parts.removeIf(part -> {
            if (!repository.isMediumLoaded(part.getMediumId())) {
                missingIds.add(part.getMediumId());
                loadingParts.add(part);
                return true;
            }
            return false;
        });

        persister.persistParts(parts);
        if (missingIds.isEmpty()) {
            return;
        }
        Utils.doPartitionedRethrow(missingIds, ids -> {
            List<ClientMedium> parents = Utils.checkAndGetBody(client.getMedia(ids));
            if (parents == null) {
                throw new NullPointerException("missing Media");
            }
            persister.persistMedia(parents);
            return false;
        });
        persister.persistParts(loadingParts);
    }

    private void persistEpisodes(Collection<ClientEpisode> episodes, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        Collection<Integer> missingIds = new HashSet<>();
        Collection<ClientEpisode> loading = new HashSet<>();

        episodes.removeIf(value -> {
            if (!repository.isPartLoaded(value.getPartId())) {
                missingIds.add(value.getPartId());
                loading.add(value);
                return true;
            }
            return false;
        });

        persister.persistEpisodes(episodes);
        if (missingIds.isEmpty()) {
            return;
        }
        Utils.doPartitionedRethrow(missingIds, ids -> {
            List<ClientPart> parents = Utils.checkAndGetBody(client.getParts(ids));
            if (parents == null) {
                throw new NullPointerException("missing Parts");
            }
            this.persistParts(parents, client, persister, repository);
            return false;
        });
        persister.persistEpisodes(loading);
    }

    private void persistReleases(Collection<ClientRelease> releases, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        Collection<Integer> missingIds = new HashSet<>();
        Collection<ClientRelease> loading = new HashSet<>();

        releases.removeIf(value -> {
            if (!repository.isEpisodeLoaded(value.getEpisodeId())) {
                missingIds.add(value.getEpisodeId());
                loading.add(value);
                return true;
            }
            return false;
        });

        persister.persistReleases(releases);
        if (missingIds.isEmpty()) {
            return;
        }
        Utils.doPartitionedRethrow(missingIds, ids -> {
            List<ClientEpisode> parents = Utils.checkAndGetBody(client.getEpisodes(ids));
            if (parents == null) {
                throw new NullPointerException("missing Episodes");
            }
            this.persistEpisodes(parents, client, persister, repository);
            return false;
        });
        persister.persistReleases(loading);
    }

    private void persistExternalLists(Collection<ClientExternalMediaList> externalMediaLists, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        Collection<String> missingIds = new HashSet<>();
        Collection<ClientExternalMediaList> loading = new HashSet<>();

        externalMediaLists.removeIf(value -> {
            if (!repository.isExternalUserLoaded(value.getUuid())) {
                missingIds.add(value.getUuid());
                loading.add(value);
                return true;
            }
            return false;
        });

        persister.persistExternalMediaLists(externalMediaLists);
        if (missingIds.isEmpty()) {
            return;
        }
        Utils.doPartitionedRethrow(missingIds, ids -> {
            List<ClientExternalUser> parents = Utils.checkAndGetBody(client.getExternalUser(ids));

            if (parents == null) {
                throw new NullPointerException("missing ExternalUser");
            }
            persister.persistExternalUsers(parents);
            return false;
        });
        persister.persistExternalMediaLists(loading);
    }

    private void syncDeleted(Client client, ClientModelPersister persister, Repository repository) throws IOException {
        ClientStat statBody = Utils.checkAndGetBody(client.getStats());

        ClientStat.ParsedStat parsedStat = statBody.parse();
        persister.persist(parsedStat);

        ReloadPart reloadPart = repository.checkReload(parsedStat);

        if (!reloadPart.loadPartEpisodes.isEmpty()) {
            Map<String, List<Integer>> partStringEpisodes = Utils.checkAndGetBody(client.getPartEpisodes(reloadPart.loadPartEpisodes));

            Map<Integer, List<Integer>> partEpisodes = mapStringToInt(partStringEpisodes);

            Collection<Integer> missingEpisodes = new HashSet<>();

            for (Map.Entry<Integer, List<Integer>> entry : partEpisodes.entrySet()) {
                for (Integer episodeId : entry.getValue()) {
                    if (!repository.isEpisodeLoaded(episodeId)) {
                        missingEpisodes.add(episodeId);
                    }
                }
            }
            if (!missingEpisodes.isEmpty()) {
                List<ClientEpisode> episodes = Utils.checkAndGetBody(client.getEpisodes(missingEpisodes));

                if (episodes == null) {
                    throw new NullPointerException("missing Episodes");
                }
                this.persistEpisodes(episodes, client, persister, repository);
            }

            persister.deleteLeftoverEpisodes(partEpisodes);

            reloadPart = repository.checkReload(parsedStat);
        }


        if (!reloadPart.loadPartReleases.isEmpty()) {
            Response<Map<String, List<ClientSimpleRelease>>> partReleasesResponse = client.getPartReleases(reloadPart.loadPartReleases);

            Map<String, List<ClientSimpleRelease>> partStringReleases = Utils.checkAndGetBody(partReleasesResponse);

            Map<Integer, List<ClientSimpleRelease>> partReleases = mapStringToInt(partStringReleases);

            Collection<Integer> missingEpisodes = new HashSet<>();

            for (Map.Entry<Integer, List<ClientSimpleRelease>> entry : partReleases.entrySet()) {
                for (ClientSimpleRelease release : entry.getValue()) {
                    if (!repository.isEpisodeLoaded(release.id)) {
                        missingEpisodes.add(release.id);
                    }
                }
            }
            if (!missingEpisodes.isEmpty()) {
                List<ClientEpisode> episodes = client.getEpisodes(missingEpisodes).body();

                if (episodes == null) {
                    throw new NullPointerException("missing Episodes");
                }
                this.persistEpisodes(episodes, client, persister, repository);
            }
            Collection<Integer> episodesToLoad = persister.deleteLeftoverReleases(partReleases);

            if (!episodesToLoad.isEmpty()) {
                Utils.doPartitionedRethrow(episodesToLoad, ids -> {
                    List<ClientEpisode> episodes = Utils.checkAndGetBody(client.getEpisodes(ids));

                    if (episodes == null) {
                        throw new NullPointerException("missing Episodes");
                    }
                    this.persistEpisodes(episodes, client, persister, repository);
                    return false;
                });
            }

            reloadPart = repository.checkReload(parsedStat);
        }

        // as even now some errors crop up, just load all this shit and dump it in 100er steps
        if (!reloadPart.loadPartEpisodes.isEmpty() || !reloadPart.loadPartReleases.isEmpty()) {
            Collection<Integer> partsToLoad = new HashSet<>();
            partsToLoad.addAll(reloadPart.loadPartEpisodes);
            partsToLoad.addAll(reloadPart.loadPartReleases);

            Utils.doPartitionedRethrow(partsToLoad, ids -> {
                List<ClientPart> parts = Utils.checkAndGetBody(client.getParts(ids));

                if (parts == null) {
                    throw new NullPointerException("missing Episodes");
                }
                this.persistParts(parts, client, persister, repository);
                return false;
            });
            reloadPart = repository.checkReload(parsedStat);
        }

        if (!reloadPart.loadPartEpisodes.isEmpty()) {
            @SuppressLint("DefaultLocale")
            String msg = String.format(
                    "Episodes of %d Parts to load even after running once",
                    reloadPart.loadPartEpisodes.size()
            );
            System.out.println(msg);
            Log.e("Repository", msg);
        }

        if (!reloadPart.loadPartReleases.isEmpty()) {
            @SuppressLint("DefaultLocale")
            String msg = String.format(
                    "Releases of %d Parts to load even after running once",
                    reloadPart.loadPartReleases.size()
            );
            System.out.println(msg);
            Log.e("Repository", msg);
        }
    }

    boolean syncWithInvalidation(Repository repository) throws IOException {
        repository.syncUser();

        if (this.isStopped()) {
            return true;
        }

        builder.setContentText("Updating Content data");
        notificationManager.notify(syncNotificationId, builder.build());
        repository.loadInvalidated();

        if (this.isStopped()) {
            return true;
        }

        builder.setContentText("Loading new Media");
        notificationManager.notify(syncNotificationId, builder.build());
        repository.loadAllMedia();
        return false;
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
