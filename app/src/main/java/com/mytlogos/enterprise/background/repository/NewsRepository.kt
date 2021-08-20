package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.TaskManager
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.News
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.checkAndGetBody
import org.joda.time.DateTime

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

    suspend fun refreshNews(latest: DateTime?) {
        val news = client.getNews(latest, null).checkAndGetBody()
        RepositoryImpl.instance.getPersister().persistNews(news)
    }

    companion object :
        SingletonHolder<NewsRepository, Application>(::NewsRepository)
}