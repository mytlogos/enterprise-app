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
import com.mytlogos.enterprise.background.ReloadStat;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.SimpleToc;
import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.NotConnectedException;
import com.mytlogos.enterprise.background.api.ServerException;
import com.mytlogos.enterprise.background.api.model.ClientChangedEntities;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientEpisodePure;
import com.mytlogos.enterprise.background.api.model.ClientEpisodeRelease;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaListPure;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientPartPure;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientToc;
import com.mytlogos.enterprise.model.Toc;
import com.mytlogos.enterprise.preferences.UserPreferences;
import com.mytlogos.enterprise.tools.Utils;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private int mediaAddedOrUpdated = 0;
    private int partAddedOrUpdated = 0;
    private int episodesAddedOrUpdated = 0;
    private int releasesAddedOrUpdated = 0;
    private int listsAddedOrUpdated = 0;
    private int externalUserAddedOrUpdated = 0;
    private int externalListAddedOrUpdated = 0;
    private int newsAddedOrUpdated = 0;
    private int mediaInWaitAddedOrUpdated = 0;
    private int totalAddedOrUpdated = 0;

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

        UserPreferences.init(application);

        try {
            Repository repository = RepositoryImpl.getInstance(application);

            if (!repository.isClientAuthenticated()) {
                cleanUp();
                return Result.retry();
            }
            this.notificationManager = NotificationManagerCompat.from(this.getApplicationContext());
            this.builder = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID);
            this.builder
                    .setContentTitle("Start Synchronizing")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            this.notificationManager.notify(this.syncNotificationId, this.builder.build());
//            if (syncWithInvalidation(repository)) return this.stopSynchronize();
            syncWithTime(repository);

            StringBuilder builder = new StringBuilder("Added or Updated:\n");
            append(builder, "Media: ", this.mediaAddedOrUpdated);
            append(builder, "Parts: ", this.partAddedOrUpdated);
            append(builder, "Episodes: ", this.episodesAddedOrUpdated);
            append(builder, "Releases: ", this.releasesAddedOrUpdated);
            append(builder, "MediaLists: ", this.listsAddedOrUpdated);
            append(builder, "ExternalUser: ", this.externalUserAddedOrUpdated);
            append(builder, "ExternalLists: ", this.externalListAddedOrUpdated);
            append(builder, "News: ", this.newsAddedOrUpdated);
            append(builder, "MediaInWait: ", this.mediaInWaitAddedOrUpdated);
            notify("Synchronization complete", builder.toString(), this.totalAddedOrUpdated, true);
        } catch (Exception e) {
            String contentText;
            if (e instanceof IOException) {
                if (e instanceof NotConnectedException) {
                    contentText = "Not connected with Server";
                } else if (e instanceof ServerException) {
                    contentText = "Response with Error Message";
                } else {
                    contentText = "Error between App and Server";
                }
            } else {
                contentText = "Local Error";
            }
            e.printStackTrace();

            StringBuilder builder = new StringBuilder("Added or Updated:\n");
            append(builder, "Media: ", this.mediaAddedOrUpdated);
            append(builder, "Parts: ", this.partAddedOrUpdated);
            append(builder, "Episodes: ", this.episodesAddedOrUpdated);
            append(builder, "Releases: ", this.releasesAddedOrUpdated);
            append(builder, "MediaLists: ", this.listsAddedOrUpdated);
            append(builder, "ExternalUser: ", this.externalUserAddedOrUpdated);
            append(builder, "ExternalLists: ", this.externalListAddedOrUpdated);
            append(builder, "News: ", this.newsAddedOrUpdated);
            append(builder, "MediaInWait: ", this.mediaInWaitAddedOrUpdated);
            notify(contentText, builder.toString(), 0, true);
            cleanUp();
            return Result.failure();
        }
        cleanUp();
        return Result.success();
    }

    private void notify(String title, String contentText, int finished, boolean changeContent) {
        this.notify(title, contentText, changeContent, finished, finished);
    }

    private void notify(String title, String contentText, boolean changeContent, int current, int total) {
        this.builder
                .setContentTitle(title)
                .setProgress(total, current, false);
        if (changeContent) {
            this.builder.setContentText(contentText);
        }
        this.notificationManager.notify(this.syncNotificationId, this.builder.build());
    }

    private void notify(String title, String contentText, boolean indeterminate, boolean changeContent) {
        this.builder
                .setContentTitle(title)
                .setProgress(0, 0, indeterminate);

        if (changeContent) {
            this.builder.setContentText(contentText);
        }
        this.notificationManager.notify(this.syncNotificationId, this.builder.build());
    }

    private boolean syncWithTime(Repository repository) throws IOException {
        Client client = repository.getClient(this);
        ClientModelPersister persister = repository.getPersister(this);

        DateTime lastSync = UserPreferences.getLastSync();
        syncChanged(lastSync, client, persister, repository);
        UserPreferences.setLastSync(DateTime.now());
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
        this.notify("Requesting New Data", null, true, true);
        Response<ClientChangedEntities> changedEntitiesResponse = client.getNew(lastSync);
        ClientChangedEntities changedEntities = Utils.checkAndGetBody(changedEntitiesResponse);

        int mediaSize = this.mediaAddedOrUpdated = changedEntities.media.size();
        int partsSize = this.partAddedOrUpdated = changedEntities.parts.size();
        int episodesSize = this.episodesAddedOrUpdated = changedEntities.episodes.size();
        int releasesSize = this.releasesAddedOrUpdated = changedEntities.releases.size();
        int listsSize = this.listsAddedOrUpdated = changedEntities.lists.size();
        int extListSize = this.externalListAddedOrUpdated = changedEntities.extLists.size();
        int extUserSize = this.externalUserAddedOrUpdated = changedEntities.extUser.size();
        int newsSize = this.newsAddedOrUpdated = changedEntities.news.size();
        int mediaInWaitSize = this.mediaInWaitAddedOrUpdated = changedEntities.mediaInWait.size();

        int total = this.totalAddedOrUpdated = mediaSize + partsSize + episodesSize + releasesSize + listsSize
                + extListSize + extUserSize + newsSize + mediaInWaitSize;

        StringBuilder builder = new StringBuilder();
        append(builder, "Media: ", mediaSize);
        append(builder, "Parts: ", partsSize);
        append(builder, "Episodes: ", episodesSize);
        append(builder, "Releases: ", releasesSize);
        append(builder, "MediaLists: ", listsSize);
        append(builder, "ExternalUser: ", extUserSize);
        append(builder, "ExternalLists: ", extListSize);
        append(builder, "News: ", newsSize);
        append(builder, "MediaInWait: ", mediaInWaitSize);
        int current = 0;
        this.notify("Received New Data", builder.toString(), true, current, total);

        this.notify("Persisting Media", null, false, current, total);
        // persist all new or updated entities, media to releases needs to be in this order
        persister.persistMedia(changedEntities.media);
        current += mediaSize;
        changedEntities.media.clear();

        this.notify("Persisting Parts", null, false, current, total);
        this.persistPartsPure(changedEntities.parts, client, persister, repository);
        current += partsSize;
        changedEntities.parts.clear();

        this.notify("Persisting Episodes", null, false, current, total);
        this.persistEpisodesPure(changedEntities.episodes, client, persister, repository);
        current += episodesSize;
        changedEntities.episodes.clear();

        this.notify("Persisting Releases", null, false, current, total);
        this.persistReleases(changedEntities.releases, client, persister, repository);
        current += releasesSize;
        changedEntities.releases.clear();

        this.notify("Persisting Lists", null, false, current, total);
        persister.persistUserLists(changedEntities.lists);
        current += listsSize;
        changedEntities.lists.clear();

        this.notify("Persisting ExternalUser", null, false, current, total);
        persister.persistExternalUsersPure(changedEntities.extUser);
        current += extUserSize;
        changedEntities.extUser.clear();

        this.notify("Persisting External Lists", null, false, current, total);
        this.persistExternalListsPure(changedEntities.extLists, client, persister, repository);
        current += extListSize;
        changedEntities.extLists.clear();

        this.notify("Persisting unused Media", null, false, current, total);
        persister.persistMediaInWait(changedEntities.mediaInWait);
        current += mediaInWaitSize;
        changedEntities.media.clear();

        this.notify("Persisting News", null, false, current, total);
        persister.persistNews(changedEntities.news);
        current += newsSize;
        changedEntities.news.clear();
        this.notify("Saved all Changes", null, false, current, total);
    }

    private void append(StringBuilder builder, String prefix, int i) {
        if (i > 0) {
            builder.append(prefix).append(i).append("\n");
        }
    }

    private void persistParts(List<ClientPart> parts, Client client, ClientModelPersister persister, Repository repository) throws IOException {
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
            List<ClientSimpleMedium> simpleMedia = parents.stream().map(ClientSimpleMedium::new).collect(Collectors.toList());
            persister.persistMedia(simpleMedia);
            return false;
        });
        persister.persistParts(loadingParts);
    }

    private void persistPartsPure(List<ClientPartPure> parts, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        List<ClientPart> unPureParts = parts
                .stream()
                .map(part -> new ClientPart(
                        part.getMediumId(),
                        part.getId(),
                        part.getTitle(),
                        part.getTotalIndex(),
                        part.getPartialIndex(),
                        null
                ))
                .collect(Collectors.toList());
        this.persistParts(unPureParts, client, persister, repository);
    }

    private void persistEpisodes(List<ClientEpisode> episodes, Client client, ClientModelPersister persister, Repository repository) throws IOException {
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

    private void persistEpisodesPure(List<ClientEpisodePure> episodes, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        List<ClientEpisode> unPure = episodes
                .stream()
                .map(part -> new ClientEpisode(
                        part.getId(),
                        part.getProgress(),
                        part.getPartId(),
                        part.getTotalIndex(),
                        part.getPartialIndex(),
                        part.getCombiIndex(),
                        part.getReadDate(),
                        new ClientEpisodeRelease[0]
                ))
                .collect(Collectors.toList());
        this.persistEpisodes(unPure, client, persister, repository);
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
    private void persistExternalLists(List<ClientExternalMediaList> externalMediaLists, Client client, ClientModelPersister persister, Repository repository) throws IOException {
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

    private void persistExternalListsPure(List<ClientExternalMediaListPure> externalMediaLists, Client client, ClientModelPersister persister, Repository repository) throws IOException {
        List<ClientExternalMediaList> unPure = externalMediaLists
                .stream()
                .map(part -> new ClientExternalMediaList(
                        part.getUuid(),
                        part.getId(),
                        part.getName(),
                        part.getMedium(),
                        part.getUrl(),
                        new int[0]
                ))
                .collect(Collectors.toList());
        this.persistExternalLists(unPure, client, persister, repository);
    }

    private void syncDeleted(Client client, ClientModelPersister persister, Repository repository) throws IOException {
        notify("Synchronize Deleted Items", null, true, false);
        ClientStat statBody = Utils.checkAndGetBody(client.getStats());

        ClientStat.ParsedStat parsedStat = statBody.parse();
        persister.persist(parsedStat);

        ReloadStat reloadStat = repository.checkReload(parsedStat);

        if (!reloadStat.loadMedium.isEmpty()) {
            final List<ClientMedium> media = Utils.checkAndGetBody(client.getMedia(reloadStat.loadMedium));
            List<ClientSimpleMedium> simpleMedia = media.stream().map(ClientSimpleMedium::new).collect(Collectors.toList());
            persister.persistMedia(simpleMedia);

            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadExUser.isEmpty()) {
            final List<ClientExternalUser> users = Utils.checkAndGetBody(client.getExternalUser(reloadStat.loadExUser));
            persister.persistExternalUsers(users);

            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadLists.isEmpty()) {
            final ClientMultiListQuery listQuery = Utils.checkAndGetBody(client.getLists(reloadStat.loadLists));
            persister.persist(listQuery);

            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadPart.isEmpty()) {
            Utils.doPartitionedRethrow(reloadStat.loadPart, partIds -> {
                final List<ClientPart> parts = Utils.checkAndGetBody(client.getParts(partIds));
                persister.persistParts(parts);
                return false;
            });
            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            Map<String, List<Integer>> partStringEpisodes = Utils.checkAndGetBody(client.getPartEpisodes(reloadStat.loadPartEpisodes));

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
                this.persistEpisodes(episodes, client, persister, repository);
            }
            Utils.doPartitionedRethrow(partEpisodes.keySet(), ids -> {

                Map<Integer, List<Integer>> currentEpisodes = new HashMap<>();
                for (Integer id : ids) {
                    currentEpisodes.put(id, Objects.requireNonNull(partEpisodes.get(id)));
                }
                persister.deleteLeftoverEpisodes(currentEpisodes);
                return false;
            });

            reloadStat = repository.checkReload(parsedStat);
        }


        if (!reloadStat.loadPartReleases.isEmpty()) {
            Response<Map<String, List<ClientSimpleRelease>>> partReleasesResponse = client.getPartReleases(reloadStat.loadPartReleases);

            Map<String, List<ClientSimpleRelease>> partStringReleases = Utils.checkAndGetBody(partReleasesResponse);

            Map<Integer, List<ClientSimpleRelease>> partReleases = mapStringToInt(partStringReleases);

            Collection<Integer> missingEpisodes = new HashSet<>();

            for (Map.Entry<Integer, List<ClientSimpleRelease>> entry : partReleases.entrySet()) {
                for (ClientSimpleRelease release : entry.getValue()) {
                    if (!repository.isEpisodeLoaded(release.getId())) {
                        missingEpisodes.add(release.getId());
                    }
                }
            }
            if (!missingEpisodes.isEmpty()) {
                List<ClientEpisode> episodes = Utils.checkAndGetBody(client.getEpisodes(missingEpisodes));
                persistEpisodes(episodes, client, persister, repository);
            }
            Collection<Integer> episodesToLoad = persister.deleteLeftoverReleases(partReleases);

            if (!episodesToLoad.isEmpty()) {
                Utils.doPartitionedRethrow(episodesToLoad, ids -> {
                    List<ClientEpisode> episodes = Utils.checkAndGetBody(client.getEpisodes(ids));
                    this.persistEpisodes(episodes, client, persister, repository);
                    return false;
                });
            }

            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadMediumTocs.isEmpty()) {
            final Response<List<ClientToc>> mediumTocsResponse = client.getMediumTocs(reloadStat.loadMediumTocs);
            final List<ClientToc> mediumTocs = Utils.checkAndGetBody(mediumTocsResponse);
            Map<Integer, List<String>> mediaTocs = new HashMap<>();

            for (ClientToc mediumToc : mediumTocs) {
                mediaTocs.computeIfAbsent(mediumToc.getMediumId(), id -> new ArrayList<>()).add(mediumToc.getLink());
            }
            Utils.doPartitionedRethrow(mediaTocs.keySet(), mediaIds-> {
                Map<Integer, List<String>> partitionedMediaTocs = new HashMap<>();

                for (Integer mediaId : mediaIds) {
                    partitionedMediaTocs.put(mediaId, Objects.requireNonNull(mediaTocs.get(mediaId)));
                }
                this.persistTocs(partitionedMediaTocs, persister);
                persister.deleteLeftoverTocs(partitionedMediaTocs);
                return false;
            });
        }

        // as even now some errors crop up, just load all this shit and dump it in 100er steps
        if (!reloadStat.loadPartEpisodes.isEmpty() || !reloadStat.loadPartReleases.isEmpty()) {
            Collection<Integer> partsToLoad = new HashSet<>();
            partsToLoad.addAll(reloadStat.loadPartEpisodes);
            partsToLoad.addAll(reloadStat.loadPartReleases);

            Utils.doPartitionedRethrow(partsToLoad, ids -> {
                List<ClientPart> parts = Utils.checkAndGetBody(client.getParts(ids));

                this.persistParts(parts, client, persister, repository);
                return false;
            });
            reloadStat = repository.checkReload(parsedStat);
        }

        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            @SuppressLint("DefaultLocale")
            String msg = String.format(
                    "Episodes of %d Parts to load even after running once",
                    reloadStat.loadPartEpisodes.size()
            );
            System.out.println(msg);
            Log.e("Repository", msg);
        }

        if (!reloadStat.loadPartReleases.isEmpty()) {
            @SuppressLint("DefaultLocale")
            String msg = String.format(
                    "Releases of %d Parts to load even after running once",
                    reloadStat.loadPartReleases.size()
            );
            System.out.println(msg);
            Log.e("Repository", msg);
        }
    }

    private void persistTocs(Map<Integer, List<String>> mediaTocs, ClientModelPersister persister) {
        List<Toc> inserts = new LinkedList<>();

        for (Map.Entry<Integer, List<String>> entry : mediaTocs.entrySet()) {
            final int mediumId = entry.getKey();

            for (String tocLink : entry.getValue()) {
                inserts.add(new SimpleToc(mediumId, tocLink));
            }
        }
        persister.persistTocs(inserts);
    }

    boolean syncWithInvalidation(Repository repository) throws IOException {
        repository.syncUser();

        if (this.isStopped()) {
            return true;
        }

        this.builder.setContentText("Updating Content data");
        this.notificationManager.notify(this.syncNotificationId, this.builder.build());
        repository.loadInvalidated();

        if (this.isStopped()) {
            return true;
        }

        this.builder.setContentText("Loading new Media");
        this.notificationManager.notify(this.syncNotificationId, this.builder.build());
        repository.loadAllMedia();
        return false;
    }

    private Result stopSynchronize() {
        this.notificationManager.notify(
                this.syncNotificationId,
                this.builder
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
        if (this.notificationManager != null) {
            this.notificationManager.cancel(this.syncNotificationId);
        }
        uuid = null;
    }

}
