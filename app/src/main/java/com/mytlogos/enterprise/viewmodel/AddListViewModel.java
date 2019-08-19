package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import com.mytlogos.enterprise.model.MediaList;

import java.io.IOException;

public class AddListViewModel extends RepoViewModel {

    AddListViewModel(Application application) {
        super(application);
    }

    public void addList(MediaList list, boolean autoDownload) throws IOException {
        this.repository.addList(list, autoDownload);
    }

    public boolean exists(String listName) {
        return this.repository.listExists(listName);
    }
}
