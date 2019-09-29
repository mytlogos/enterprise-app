package com.mytlogos.enterprise.background;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;
import androidx.collection.ArraySet;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.NotConnectedException;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.model.ExternalMediaListSetting;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.tools.Utils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

@SuppressLint("UseSparseArrays")
class EditService {
    static final int USER = 1;
    static final int EXTERNAL_USER = 2;
    static final int EXTERNAL_LIST = 3;
    static final int LIST = 4;
    static final int MEDIUM = 5;
    static final int PART = 6;
    static final int EPISODE = 7;
    static final int RELEASE = 8;
    static final int NEWS = 9;

    @IntDef(value = {
            USER,
            EXTERNAL_LIST,
            EXTERNAL_USER,
            LIST,
            MEDIUM,
            PART,
            EPISODE,
            RELEASE,
            NEWS
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface EditObject {
    }


    static final int ADD = 1;
    static final int REMOVE = 2;
    static final int MOVE = 3;
    static final int ADD_TO = 4;
    static final int REMOVE_FROM = 5;
    static final int MERGE = 6;
    static final int CHANGE_NAME = 7;
    static final int CHANGE_TYPE = 8;
    static final int ADD_TOC = 9;
    static final int REMOVE_TOC = 10;
    static final int CHANGE_PROGRESS = 11;
    static final int CHANGE_READ = 12;

    @IntDef(value = {
            ADD,
            REMOVE,
            MOVE,
            ADD_TO,
            REMOVE_FROM,
            MERGE,
            CHANGE_NAME,
            CHANGE_TYPE,
            ADD_TOC,
            REMOVE_TOC,
            CHANGE_PROGRESS,
            CHANGE_READ
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Event {
    }

    private final Client client;
    private final DatabaseStorage storage;
    private final ClientModelPersister persister;
    private final ExecutorService pushEditExecutor = Executors.newSingleThreadExecutor();

    EditService(Client client, DatabaseStorage storage, ClientModelPersister persister) {
        this.client = client;
        this.storage = storage;
        this.persister = persister;
        this.client.addDisconnectedListener(timeDisconnected -> this.pushEditExecutor.execute(this::publishEditEvents));
    }

    private void publishEditEvents() {
        List<? extends EditEvent> events = this.storage.getEditEvents();
        if (events.isEmpty()) {
            return;
        }
        events.sort(Comparator.comparing(EditEvent::getDateTime));
        Map<Integer, Map<Integer, List<EditEvent>>> objectTypeEventMap = new HashMap<>();

        for (EditEvent event : events) {
            objectTypeEventMap
                    .computeIfAbsent(event.getObjectType(), integer -> new HashMap<>())
                    .computeIfAbsent(event.getEventType(), integer -> new ArrayList<>())
                    .add(event);
        }
        Collection<EditEvent> consumedEvents = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<EditEvent>>> entry : objectTypeEventMap.entrySet()) {
            try {
                boolean consumed = true;
                switch (entry.getKey()) {
                    case USER:
                        this.publishUserEvents(entry.getValue());
                        break;
                    case EXTERNAL_USER:
                        this.publishExternalUserEvents(entry.getValue());
                        break;
                    case EXTERNAL_LIST:
                        this.publishExternalListEvents(entry.getValue());
                        break;
                    case LIST:
                        this.publishListEvents(entry.getValue());
                        break;
                    case MEDIUM:
                        this.publishMediumEvents(entry.getValue());
                        break;
                    case PART:
                        this.publishPartEvents(entry.getValue());
                        break;
                    case EPISODE:
                        this.publishEpisodeEvents(entry.getValue());
                        break;
                    case RELEASE:
                        this.publishReleaseEvents(entry.getValue());
                        break;
                    case NEWS:
                        this.publishNewsEvents(entry.getValue());
                        break;
                    default:
                        consumed = false;
                        System.err.println("unknown event object type: " + entry.getKey());
                }
                if (consumed) {
                    for (List<EditEvent> value : entry.getValue().values()) {
                        consumedEvents.addAll(value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.storage.removeEditEvents(consumedEvents);
    }

    private void publishUserEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    private void publishExternalUserEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    private void publishListEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    private void publishExternalListEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    private void publishMediumEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        for (Map.Entry<Integer, List<EditEvent>> entry : typeEventsMap.entrySet()) {
            List<EditEvent> value = entry.getValue();

            // TODO: 26.09.2019 implement
        }
    }

    private void publishPartEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
    }

    private void publishEpisodeEvents(Map<Integer, List<EditEvent>> typeEventsMap) throws NotConnectedException {
        for (Map.Entry<Integer, List<EditEvent>> entry : typeEventsMap.entrySet()) {
            List<EditEvent> value = entry.getValue();

            if (entry.getKey() == CHANGE_PROGRESS) {
                this.publishEpisodeProgress(value);
            }
        }
        // TODO: 26.09.2019 implement
    }

    private void publishEpisodeProgress(List<EditEvent> value) throws NotConnectedException {
        Map<Integer, EditEvent> latestProgress = new HashMap<>();
        Map<Integer, EditEvent> earliestProgress = new HashMap<>();

        for (EditEvent event : value) {
            latestProgress.merge(event.getId(), event, (editEvent, editEvent2) -> {
                if (editEvent2.getDateTime().isAfter(editEvent.getDateTime())) {
                    return editEvent2;
                } else {
                    return editEvent;
                }
            });
            earliestProgress.merge(event.getId(), event, (editEvent, editEvent2) -> {
                if (editEvent2.getDateTime().isBefore(editEvent.getDateTime())) {
                    return editEvent2;
                } else {
                    return editEvent;
                }
            });
        }
        Map<Float, Set<Integer>> currentProgressEpisodeMap = new HashMap<>();

        for (Map.Entry<Integer, EditEvent> latestEntry : latestProgress.entrySet()) {
            String newValue = latestEntry.getValue().getSecondValue();

            float newProgress = this.parseProgress(newValue);
            currentProgressEpisodeMap
                    .computeIfAbsent(newProgress, aFloat -> new ArraySet<>())
                    .add(latestEntry.getKey());
        }

        for (Map.Entry<Float, Set<Integer>> progressEntry : currentProgressEpisodeMap.entrySet()) {
            Float progress = progressEntry.getKey();
            Set<Integer> ids = progressEntry.getValue();

            try {
                if (!this.updateProgressOnline(progress, ids)) {
                    Map<Float, Set<Integer>> progressMap = new HashMap<>();

                    for (Integer id : ids) {
                        EditEvent event = earliestProgress.get(id);
                        if (event == null) {
                            throw new IllegalStateException("expected a value, not null for: " + id);
                        }
                        float idProgress = this.parseProgress(event.getFirstValue());
                        progressMap.computeIfAbsent(idProgress, aFloat -> new HashSet<>()).add(id);
                    }
                    progressMap.forEach((updateProgress, progressIds) -> this.storage.updateProgress(progressIds, updateProgress));
                }
            } catch (NotConnectedException e) {
                throw new NotConnectedException(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private float parseProgress(String value) {
        float progress;
        try {
            progress = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            progress = Boolean.parseBoolean(value) ? 1 : 0;
        }
        return progress;
    }

    private void publishReleaseEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    private void publishNewsEvents(Map<Integer, List<EditEvent>> typeEventsMap) {
        System.out.println(typeEventsMap);
        // TODO: 26.09.2019 implement
    }

    void updateUser(UpdateUser updateUser) {
        TaskManager.runTask(() -> {
            User value = this.storage.getUserNow();

            if (value == null) {
                throw new IllegalArgumentException("cannot change user when none is logged in");
            }
            ClientUpdateUser user = new ClientUpdateUser(
                    value.getUuid(), updateUser.getName(),
                    updateUser.getPassword(),
                    updateUser.getNewPassword()
            );
            if (!this.client.isOnline()) {
                System.err.println("offline user edits are not allowed");
                return;
            }
            try {
                Boolean body = this.client.updateUser(user).body();

                if (body != null && body) {
                    this.persister.persist(user);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType) {
        return TaskManager.runCompletableTask(() -> {
            if (listSetting instanceof ExternalMediaListSetting) {
                return "Cannot update External Lists";
            }
            int listId = listSetting.getListId();
            ClientMediaList mediaList = new ClientMediaList(
                    listSetting.getUuid(),
                    listId,
                    listSetting.getName(),
                    newMediumType,
                    new int[0]
            );
            return this.updateList(listSetting.getName(), newMediumType, listId, mediaList);
        });
    }

    CompletableFuture<String> updateListName(MediaListSetting listSetting, String newName) {
        return TaskManager.runCompletableTask(() -> {
            if (listSetting instanceof ExternalMediaListSetting) {
                return "Cannot update External Lists";
            }
            int listId = listSetting.getListId();
            ClientMediaList mediaList = new ClientMediaList(
                    listSetting.getUuid(),
                    listId,
                    newName,
                    listSetting.getMedium(),
                    new int[0]
            );
            return updateList(newName, listSetting.getMedium(), listId, mediaList);
        });
    }

    private String updateList(String newName, int newMediumType, int listId, ClientMediaList mediaList) {
        try {
            if (!this.client.isOnline()) {
                MediaListSetting setting = this.storage.getListSettingNow(listId, false);

                if (setting == null) {
                    return "Not available in storage";
                }
                List<EditEvent> editEvents = new ArrayList<>();

                if (!Objects.equals(setting.getName(), newName)) {
                    editEvents.add(new EditEventImpl(listId, MEDIUM, CHANGE_NAME, setting.getName(), newName));
                }
                if (setting.getMedium() != newMediumType) {
                    editEvents.add(new EditEventImpl(listId, MEDIUM, CHANGE_TYPE, setting.getMedium(), newMediumType));
                }
                this.storage.insertEditEvent(editEvents);
                this.persister.persist(mediaList).finish();
                return "";
            }
            this.client.updateList(mediaList);
            ClientListQuery query = this.client.getList(listId).body();
            this.persister.persist(query).finish();
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not update List";
        }
        return "";
    }

    CompletableFuture<String> updateMedium(MediumSetting mediumSettings) {
        return TaskManager.runCompletableTask(() -> {
            int mediumId = mediumSettings.getMediumId();
            ClientMedium clientMedium = new ClientMedium(
                    new int[0],
                    new int[0],
                    mediumSettings.getCurrentRead(),
                    new int[0],
                    mediumId,
                    mediumSettings.getCountryOfOrigin(),
                    mediumSettings.getLanguageOfOrigin(),
                    mediumSettings.getAuthor(),
                    mediumSettings.getTitle(),
                    mediumSettings.getMedium(),
                    mediumSettings.getArtist(),
                    mediumSettings.getLang(),
                    mediumSettings.getStateOrigin(),
                    mediumSettings.getStateTL(),
                    mediumSettings.getSeries(),
                    mediumSettings.getUniverse()
            );

            if (!this.client.isOnline()) {
                MediumSetting setting = this.storage.getMediumSettingsNow(mediumId);

                if (setting == null) {
                    return "Not available in storage";
                }
                List<EditEvent> editEvents = new ArrayList<>();

                if (!Objects.equals(setting.getTitle(), mediumSettings.getTitle())) {
                    editEvents.add(new EditEventImpl(mediumId, MEDIUM, CHANGE_NAME, setting.getTitle(), mediumSettings.getTitle()));
                }
                if (setting.getMedium() != mediumSettings.getMedium()) {
                    editEvents.add(new EditEventImpl(mediumId, MEDIUM, CHANGE_TYPE, setting.getMedium(), mediumSettings.getMedium()));
                }
                this.storage.insertEditEvent(editEvents);
                this.persister.persist(clientMedium).finish();
            }
            try {
                this.client.updateMedia(clientMedium);
                ClientMedium medium = this.client.getMedium(mediumId).body();
                this.persister.persist(medium).finish();
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not update Medium";
            }
            return "";
        });
    }


    void updateRead(Collection<Integer> episodeIds, boolean read) throws Exception {
        float progress = read ? 1f : 0f;
        Utils.doPartitioned(episodeIds, ids -> {
            if (!this.client.isOnline()) {
                List<Integer> filteredIds = this.storage.getReadEpisodes(episodeIds, !read);

                if (filteredIds.isEmpty()) {
                    return false;
                }
                Collection<EditEvent> events = new ArrayList<>(filteredIds.size());
                for (Integer id : filteredIds) {
                    events.add(new EditEventImpl(id, EPISODE, CHANGE_PROGRESS, null, progress));
                }
                this.storage.insertEditEvent(events);
                this.storage.updateProgress(filteredIds, progress);
                return false;
            }
            return !updateProgressOnline(progress, ids);
        });
    }

    private boolean updateProgressOnline(float progress, Collection<Integer> ids) throws IOException {
        Response<Boolean> response = this.client.addProgress(ids, progress);

        if (!response.isSuccessful() || response.body() == null || !response.body()) {
            return false;
        }
        this.storage.updateProgress(ids, progress);
        return true;
    }

    CompletableFuture<Boolean> removeItemFromList(int listId, Collection<Integer> mediumIds) {
        return TaskManager.runCompletableTask(() -> {
            try {
                if (!this.client.isOnline()) {
                    Collection<EditEvent> events = new ArrayList<>(mediumIds.size());

                    for (Integer id : mediumIds) {
                        EditEvent event = new EditEventImpl(id, MEDIUM, REMOVE_FROM, listId, null);
                        events.add(event);
                    }
                    this.storage.insertEditEvent(events);
                    this.storage.removeItemFromList(listId, mediumIds);
                    this.storage.insertDanglingMedia(mediumIds);
                    return true;
                }

                Response<Boolean> response = this.client.deleteListMedia(listId, mediumIds);
                Boolean success = response.body();

                if (success != null && success) {
                    this.storage.removeItemFromList(listId, mediumIds);
                    this.storage.insertDanglingMedia(mediumIds);
                    return true;
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    CompletableFuture<Boolean> addMediumToList(int listId, Collection<Integer> ids) {
        return TaskManager.runCompletableTask(() -> {
            try {
                // to prevent duplicates
                Collection<Integer> items = this.storage.getListItems(listId);
                ids.removeAll(items);

                // adding nothing cannot fail
                if (ids.isEmpty()) {
                    return true;
                }
                if (!this.client.isOnline()) {
                    Collection<EditEvent> events = new ArrayList<>(ids.size());

                    for (Integer id : ids) {
                        EditEvent event = new EditEventImpl(id, MEDIUM, ADD_TO, null, listId);
                        events.add(event);
                    }
                    this.storage.insertEditEvent(events);
                    this.storage.addItemsToList(listId, ids);
                    return true;
                }

                Response<Boolean> response = this.client.addListMedia(listId, ids);
                if (response.body() == null || !response.body()) {
                    return false;
                }
                this.storage.addItemsToList(listId, ids);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    CompletableFuture<Boolean> moveItemFromList(int oldListId, int newListId, int mediumId) {
        return TaskManager.runCompletableTask(() -> {
            try {
                if (!this.client.isOnline()) {
                    EditEvent event = new EditEventImpl(mediumId, MEDIUM, MOVE, oldListId, newListId);
                    this.storage.insertEditEvent(event);
                    this.storage.removeItemFromList(oldListId, mediumId);
                    this.storage.addItemsToList(newListId, Collections.singleton(mediumId));
                    return true;
                }
                Response<Boolean> response = this.client.updateListMedia(oldListId, newListId, mediumId);
                Boolean success = response.body();

                if (success != null && success) {
                    this.storage.removeItemFromList(oldListId, mediumId);
                    this.storage.addItemsToList(newListId, Collections.singleton(mediumId));
                    return true;
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    CompletableFuture<Boolean> moveMediaToList(int oldListId, int listId, Collection<Integer> ids) {
        return TaskManager.runCompletableTask(() -> {
            try {
                // to prevent duplicates
                Collection<Integer> items = this.storage.getListItems(listId);
                ids.removeAll(items);

                // adding nothing cannot fail
                if (ids.isEmpty()) {
                    return true;
                }
                if (!this.client.isOnline()) {
                    Collection<EditEvent> events = new ArrayList<>(ids.size());

                    for (Integer id : ids) {
                        EditEvent event = new EditEventImpl(id, MEDIUM, MOVE, oldListId, listId);
                        events.add(event);
                    }
                    this.storage.insertEditEvent(events);
                    this.storage.moveItemsToList(oldListId, listId, ids);
                    return true;
                }

                Collection<Integer> successMove = new ArrayList<>();

                for (Integer id : ids) {
                    Response<Boolean> response = this.client.updateListMedia(oldListId, listId, id);
                    Boolean success = response.body();

                    if (success != null && success) {
                        successMove.add(id);
                    }
                }
                this.storage.moveItemsToList(oldListId, listId, successMove);
                return !successMove.isEmpty();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
