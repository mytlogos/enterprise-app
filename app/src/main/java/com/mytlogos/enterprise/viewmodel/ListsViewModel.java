package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.ToDownload;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListsViewModel extends RepoViewModel {

    private LiveData<? extends MediaListSetting> listSettings;
    private LiveData<MediumSetting> settings;

    public ListsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<MediaList>> getLists() {
        return repository.getLists();
    }

    public LiveData<List<MediaList>> getInternLists() {
        return repository.getInternLists();
    }

    public LiveData<? extends MediaListSetting> getListSettings(int id, boolean isExternal) {
        if (this.listSettings == null) {
            listSettings = repository.getListSettings(id, isExternal);
        }
        return listSettings;
    }

    public CompletableFuture<String> updateListName(MediaListSetting listSetting, String text) {
        return repository.updateListName(listSetting, text);
    }

    public CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType) {
        return repository.updateListMedium(listSetting, newMediumType);
    }

    public void updateToDownload(boolean add, ToDownload toDownload) {
        TaskManager.runTask(() -> repository.updateToDownload(add, toDownload));
    }

    public LiveData<MediumSetting> getMediumSettings(int mediumId) {
        if (this.settings == null) {
            this.settings = repository.getMediumSettings(mediumId);
        }
        return this.settings;
    }

    public CompletableFuture<String> updateMedium(MediumSetting mediumSettings) {
        return repository.updateMediumType(mediumSettings);
    }

    public CompletableFuture<Boolean> moveMediumToList(int oldListId, int newListId, Collection<Integer> ids) {
        return repository.moveMediaToList(oldListId, newListId, ids);
    }

    public CompletableFuture<Boolean> addMediumToList(int listId, Collection<Integer> ids) {
        return repository.addMediumToList(listId, ids);
    }
}
