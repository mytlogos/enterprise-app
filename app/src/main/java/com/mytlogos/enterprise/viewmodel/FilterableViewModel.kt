package com.mytlogos.enterprise.viewmodel

import android.app.Application

abstract class FilterableViewModel internal constructor(application: Application) :
    RepoViewModel(application) {
    fun processStringFilter(filter: String): String {
        return filter.lowercase()
    }

    abstract fun resetFilter()
}