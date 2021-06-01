package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.ReadEpisode;

public class ReadEpisodeViewModel extends RepoViewModel {
    public ReadEpisodeViewModel(Application application) {
        super(application);
    }

    public LiveData<PagedList<ReadEpisode>> getReadEpisodes() {
        return repository.getReadTodayEpisodes();
    }

}
