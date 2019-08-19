package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.tools.Sortings;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListMediaViewModel extends RepoViewModel {

    private LiveData<List<MediumItem>> items;

    public ListMediaViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<MediumItem>> getMedia(int listId, boolean isExternal) {
        if (this.items == null) {
            this.items = repository.getMediumItems(listId, isExternal);
        }
        return this.items;
    }

    public CompletableFuture<Boolean> removeMedia(int listId, int mediumId) {
        return this.repository.removeItemFromList(listId, mediumId);
    }

    public CompletableFuture<Boolean> removeMedia(int listId, Collection<Integer> mediumId) {
        return this.repository.removeItemFromList(listId, mediumId);
    }

    public void setSort(Sortings sortings) {

    }
}
