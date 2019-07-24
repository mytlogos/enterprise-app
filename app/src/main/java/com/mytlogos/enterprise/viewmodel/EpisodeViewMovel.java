package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.DisplayUnreadEpisode;

import java.util.List;

public class EpisodeViewMovel extends RepoViewModel {

    EpisodeViewMovel(Application application) {
        super(application);
    }

    public LiveData<List<DisplayUnreadEpisode>> getEpisodes(int mediumId) {
        return null;
    }
}
