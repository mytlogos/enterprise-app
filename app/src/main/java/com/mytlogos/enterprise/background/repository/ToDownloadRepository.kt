package com.mytlogos.enterprise.background.repository

import android.app.Application
import com.mytlogos.enterprise.background.ClientModelPersister
import com.mytlogos.enterprise.background.EditService
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.RoomConverter
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.tools.SingletonHolder

@Suppress("BlockingMethodInNonBlockingContext")
class ToDownloadRepository private constructor(application: Application) {
    private val toDownloadDao = AbstractDatabase.getInstance(application).toDownloadDao()

    private val editService: EditService
        get() = (RepositoryImpl.instance as RepositoryImpl).editService

    private val persister: ClientModelPersister
        get() = (RepositoryImpl.instance as RepositoryImpl).getPersister()


    suspend fun getToDownloads(): List<ToDownload> {
        return RoomConverter().convertRoomToDownload(toDownloadDao.getAll())
    }

    suspend fun addToDownload(toDownload: ToDownload) {
        persister.persist(toDownload).finish()
    }

    suspend fun removeToDownloads(toDownloads: Collection<ToDownload>) {
        for (toDownload in RoomConverter().convertToDownload(toDownloads)) {
            toDownloadDao.deleteToDownload(
                toDownload.mediumId ?: 0,
                toDownload.listId ?: 0,
                toDownload.externalListId ?: 0
            )
        }
    }

    suspend fun updateToDownload(add: Boolean, toDownload: ToDownload) {
        if (add) {
            toDownloadDao.insert(RoomConverter().convert(toDownload))
        } else {
            toDownloadDao.deleteToDownload(
                toDownload.mediumId,
                toDownload.listId,
                toDownload.externalListId
            )
        }
    }

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    companion object : SingletonHolder<ToDownloadRepository, Application>(::ToDownloadRepository)
}