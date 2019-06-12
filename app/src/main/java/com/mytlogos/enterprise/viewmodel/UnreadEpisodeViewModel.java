package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.UnreadEpisode;

import java.util.List;

public class UnreadEpisodeViewModel extends AndroidViewModel {

    private final Repository repository;

    public UnreadEpisodeViewModel(@NonNull Application application) {
        super(application);
        repository = RepositoryImpl.getInstance(application);
    }

    public LiveData<List<UnreadEpisode>> getUnreadEpisodes() {
        return repository.getUnReadEpisodes();
    }
}
