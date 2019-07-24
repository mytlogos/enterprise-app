package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.MediumInWait;

import java.io.IOException;
import java.util.List;

public class MediumInWaitViewModel extends RepoViewModel{
    public MediumInWaitViewModel(Application application) {
        super(application);
    }

    public LiveData<List<MediumInWait>> getAllMediaInWait() {
        return repository.getAllMediaInWait();
    }

    public void loadMediaInWait() throws IOException {
        this.repository.loadMediaInWaitSync();
    }
}
