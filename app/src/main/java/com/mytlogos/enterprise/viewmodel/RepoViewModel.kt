package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mytlogos.enterprise.background.Repository
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance

open class RepoViewModel(application: Application) : AndroidViewModel(application) {
    val repository: Repository = getInstance(application)
}