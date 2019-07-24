package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;

class RepoViewModel extends AndroidViewModel {
    final Repository repository;

    RepoViewModel(Application application) {
        super(application);
        this.repository = RepositoryImpl.getInstance(application);
    }
}
