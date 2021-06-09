package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.paging.PagingData
import com.mytlogos.enterprise.model.ExternalUser
import kotlinx.coroutines.flow.Flow

class ExternalUserViewModel(application: Application) : RepoViewModel(application) {
    val externalUser: Flow<PagingData<ExternalUser>>
        get() = repository.externalUser
}