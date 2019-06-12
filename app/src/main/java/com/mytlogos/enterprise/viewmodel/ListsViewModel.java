package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.ToDownload;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListsViewModel extends AndroidViewModel {
    private final Repository repository;

    public ListsViewModel(@NonNull Application application) {
        super(application);
        repository = RepositoryImpl.getInstance(application);
    }

    public LiveData<List<MediaList>> getLists() {
        return repository.getLists();
    }

    public LiveData<? extends MediaListSetting> getListSettings(int id, boolean isExternal) {
        return repository.getListSettings(id, isExternal);
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
        return repository.getMediumSettings(mediumId);
    }

    public CompletableFuture<String> updateMedium(MediumSetting mediumSettings) {
        return repository.updateMediumType(mediumSettings);
    }
}
