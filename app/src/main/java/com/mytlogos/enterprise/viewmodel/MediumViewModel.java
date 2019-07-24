package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.TocPart;

import java.util.List;

public class MediumViewModel extends RepoViewModel {

    public MediumViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<MediumItem>> getAllMedia() {
        return repository.getAllMedia();
    }

    public LiveData<List<TocPart>> getToc(int mediumId) {
        return repository.getToc(mediumId);
    }
}
