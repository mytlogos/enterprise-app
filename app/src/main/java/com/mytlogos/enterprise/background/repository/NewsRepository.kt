package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.News
import com.mytlogos.enterprise.model.SimpleMedium
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.Utils
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class NewsRepository private constructor(application: Application) {
    private val newsDao = AbstractDatabase.getInstance(application).newsDao()

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    val news: LiveData<PagedList<News>>
        get() = LivePagedListBuilder(newsDao.news, 50).build()

    fun removeOldNews() {
        TaskManager.runTaskSuspend { newsDao.deleteOldNews() }
    }

    fun refreshNews(latest: DateTime?) {
        val news = Utils.checkAndGetBody(
            client.getNews(latest, null)
        )
        RepositoryImpl.instance.getPersister().persistNews(news)
    }

    companion object :
        SingletonHolder<NewsRepository, Application>(::NewsRepository)
}