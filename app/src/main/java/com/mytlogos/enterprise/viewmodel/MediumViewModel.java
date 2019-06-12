package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.MediumItem;

import java.util.List;

public class MediumViewModel extends AndroidViewModel {

    private final Repository repository;

    public MediumViewModel(@NonNull Application application) {
        super(application);
        this.repository = RepositoryImpl.getInstance(application);
    }

    public LiveData<List<MediumItem>> getAllMedia() {
        return repository.getAllMedia();
    }
}
