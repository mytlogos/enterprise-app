package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomNotification
import com.mytlogos.enterprise.model.NotificationItem
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.transformFlow
import kotlinx.coroutines.flow.Flow

@Suppress("BlockingMethodInNonBlockingContext")
class NotificationRepository private constructor(application: Application) {
    private val notificationDao = AbstractDatabase.getInstance(application).notificationDao()

    suspend fun clearNotifications() {
        notificationDao.deleteAll()
    }

    suspend fun addNotification(notification: NotificationItem) {
        notificationDao.insert(RoomNotification(
            notification.title,
            notification.description,
            notification.dateTime
        ))
    }

    val notifications
        get(): Flow<PagingData<NotificationItem>> = transformFlow { notificationDao.notifications }

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    companion object : SingletonHolder<NotificationRepository, Application>(::NotificationRepository)
}