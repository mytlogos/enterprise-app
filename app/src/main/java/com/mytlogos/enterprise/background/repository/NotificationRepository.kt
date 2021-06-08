package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class NotificationRepository private constructor(application: Application) {
    private val notificationDao = AbstractDatabase.getInstance(application).notificationDao()

    suspend fun clearNotifications() {
        notificationDao.deleteAll()
    }

    val notifications
        get(): Flow<PagingData<NotificationItem>> {
            return Pager(
                PagingConfig(50),
                pagingSourceFactory = { notificationDao.notifications }
            ).flow
        }

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    companion object : SingletonHolder<NotificationRepository, Application>(::NotificationRepository)
}