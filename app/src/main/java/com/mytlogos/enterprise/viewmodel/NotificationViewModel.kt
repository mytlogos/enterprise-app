package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.TaskManager.Companion.runTask
import com.mytlogos.enterprise.model.NotificationItem

class NotificationViewModel(application: Application) : RepoViewModel(application) {
    val notifications: LiveData<PagedList<NotificationItem>>
        get() = repository.notifications

    fun clearNotifications() {
        runTask { repository.clearNotifications() }
    }
}