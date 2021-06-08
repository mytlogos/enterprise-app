package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mytlogos.enterprise.background.TaskManager.Companion.runTask
import com.mytlogos.enterprise.background.repository.NotificationRepository
import com.mytlogos.enterprise.model.NotificationItem
import kotlinx.coroutines.flow.Flow

class NotificationViewModel(application: Application) : RepoViewModel(application) {
    val notificationRepository by lazy { NotificationRepository.getInstance(application) }
    val notifications by lazy { notificationRepository.notifications.cachedIn(viewModelScope) }

    suspend fun clearNotifications() {
        notificationRepository.clearNotifications()
    }
}