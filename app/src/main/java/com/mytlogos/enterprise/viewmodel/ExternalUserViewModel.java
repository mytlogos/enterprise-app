package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.ExternalUser;

public class ExternalUserViewModel extends RepoViewModel {

    ExternalUserViewModel(Application application) {
        super(application);
    }

    public LiveData<PagedList<ExternalUser>> getExternalUser() {
        return this.repository.getExternalUser();
    }
}
