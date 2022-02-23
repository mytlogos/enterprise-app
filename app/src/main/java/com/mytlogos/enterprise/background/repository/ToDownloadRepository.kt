package com.mytlogos.enterprise.background.repository

import android.app.Application
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.EditService
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
        return toDownloadDao.getAll().fromRoomToDownload()
    }

    suspend fun addToDownload(toDownload: ToDownload) {
        persister.persist(toDownload).finish()
    }

    suspend fun removeToDownloads(toDownloads: Collection<ToDownload>) {
        for (toDownload in toDownloads.toRoomToDownload()) {
            toDownloadDao.deleteToDownload(
                toDownload.mediumId ?: 0,
                toDownload.listId ?: 0,
                toDownload.externalListId ?: 0
            )
        }
    }

    suspend fun updateToDownload(add: Boolean, toDownload: ToDownload) {
        if (add) {
            toDownloadDao.insert(toDownload.toRoom())
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