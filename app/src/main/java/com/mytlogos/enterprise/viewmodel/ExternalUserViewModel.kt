package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.model.ExternalUser

class ExternalUserViewModel(application: Application) : RepoViewModel(application) {
    val externalUser: LiveData<PagedList<ExternalUser>>
        get() = repository.externalUser
}