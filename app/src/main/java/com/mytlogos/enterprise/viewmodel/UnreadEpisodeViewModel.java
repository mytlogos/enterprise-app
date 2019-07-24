package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.DisplayUnreadEpisode;

import java.util.List;

public class UnreadEpisodeViewModel extends RepoViewModel {

    public UnreadEpisodeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<DisplayUnreadEpisode>> getUnreadEpisodes() {
        return repository.getUnReadEpisodes();
    }
}
