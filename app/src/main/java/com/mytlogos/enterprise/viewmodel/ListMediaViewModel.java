package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.MediumItem;

import java.util.List;

public class ListMediaViewModel extends RepoViewModel {
    public ListMediaViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<MediumItem>> getMedia(int listId, boolean isExternal) {
        return repository.getMediumItems(listId, isExternal);
    }
}
