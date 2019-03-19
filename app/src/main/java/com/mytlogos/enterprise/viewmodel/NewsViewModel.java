package com.mytlogos.enterprise.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.model.News;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private final Repository repository;

    public NewsViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        repository.getNews();
    }

    public LiveData<List<News>> getNews() {
        return repository.getNews();
    }

    public void deleteOldNews() {
        repository.removeOldNews();
    }
}
