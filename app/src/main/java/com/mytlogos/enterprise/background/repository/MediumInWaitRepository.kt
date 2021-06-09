package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mytlogos.enterprise.background.ClientModelPersister
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.RoomConverter
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.SimpleMedium
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.checkAndGetBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class MediumInWaitRepository private constructor(application: Application) {
    private val mediumInWaitDao = AbstractDatabase.getInstance(application).roomMediumInWaitDao()
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()
    private val mediumDao = AbstractDatabase.getInstance(application).mediumDao()

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    private val persister: ClientModelPersister
        get() = (RepositoryImpl.instance as RepositoryImpl).getPersister()

    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<MutableList<MediumInWait>> {
        return mediumInWaitDao.getSimilar(mediumInWait.title, mediumInWait.medium)
    }

    fun getMediaSuggestions(
        title: String,
        medium: Int,
    ): LiveData<MutableList<SimpleMedium>> {
        return mediumDao.getSuggestions(title, medium)
    }

    fun getMediaInWaitSuggestions(
        title: String,
        medium: Int,
    ): LiveData<MutableList<MediumInWait>> {
        return mediumInWaitDao.getSuggestions(title, medium)
    }

    fun getMediaInWaitBy(
        filter: String?,
        mediumFilter: Int,
        hostFilter: String?,
        sortings: Sortings,
    ): Flow<PagingData<MediumInWait>> {
        var sortValue = sortings.sortValue
        val query = if (sortValue < 0) {
            sortValue = -sortValue
            mediumInWaitDao::getByDesc
        } else {
            mediumInWaitDao::getByAsc
        }
        val converter = RoomConverter()
        return Pager(
            PagingConfig(50),
            pagingSourceFactory = { query(
                sortValue,
                filter,
                mediumFilter,
                hostFilter
            )}
        ).flow.map { it.map(converter::convert) }
    }

    suspend fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>,
    ): Boolean {
        val others: MutableCollection<ClientMediumInWait> = HashSet()
        mediumInWaits.map {
            ClientMediumInWait(
                it.title,
                it.medium,
                it.link
        )}
        return try {
            val success = client.consumeMediumInWait(selectedMedium.mediumId, others).body()
            if (success != null && success) {
                val converter = RoomConverter()
                mediumInWaitDao.deleteBulk(converter.convertMediaInWait(mediumInWaits))
                true
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList,
    ): Boolean {
        val medium = ClientMediumInWait(
            mediumInWait.title,
            mediumInWait.medium,
            mediumInWait.link
        )
        val others: MutableCollection<ClientMediumInWait> = HashSet()
        for (inWait in mediumInWaits) {
            others.add(
                ClientMediumInWait(
                    inWait.title,
                    inWait.medium,
                    inWait.link
                )
            )
        }
        val listId = list.listId
        try {
            val clientMedium = client.createFromMediumInWait(
                medium,
                others,
                listId
            ).body() ?: return false

            RepositoryImpl.instance.getPersister().persist(ClientSimpleMedium(clientMedium))

            val toDelete: MutableCollection<MediumInWait> = HashSet()
            toDelete.add(mediumInWait)
            toDelete.addAll(mediumInWaits)

            val converter = RoomConverter()
            mediumInWaitDao.deleteBulk(converter.convertMediaInWait(toDelete))

            if (listId > 0) {
                mediaListDao.addJoin(listOf(RoomMediaList.MediaListMediaJoin(listId, listId)))
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun loadMediaInWaitSync() {
        val medium = checkAndGetBody(client.mediumInWait)

        if (medium.isNotEmpty()) {
            mediumInWaitDao.clear()
            persister.persistMediaInWait(medium)
        }
    }

    companion object :
        SingletonHolder<MediumInWaitRepository, Application>(::MediumInWaitRepository)
}