package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mytlogos.enterprise.background.repository.NotificationRepository

class NotificationViewModel(application: Application) : RepoViewModel(application) {
    val notificationRepository by lazy { NotificationRepository.getInstance(application) }
    val notifications by lazy { notificationRepository.notifications.cachedIn(viewModelScope) }

    suspend fun clearNotifications() {
        notificationRepository.clearNotifications()
    }
}