package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class FilterableViewModel internal constructor(application: Application) :
    AndroidViewModel(application) {
    fun processStringFilter(filter: String): String {
        return filter.lowercase()
    }

    abstract fun resetFilter()
}