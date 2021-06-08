package com.mytlogos.enterprise.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.TaskManager.Companion.runTask
import com.mytlogos.enterprise.background.repository.NewsRepository
import com.mytlogos.enterprise.model.News
import org.joda.time.DateTime

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val handler: Handler
    private val LOADING_COMPLETE = 0x1
    private val loadingComplete = MutableLiveData<Boolean>()
    private val newsRepository by lazy { NewsRepository.getInstance(application) }

    val news: LiveData<PagedList<News>> by lazy {
        newsRepository.news
    }

    fun deleteOldNews() {
        newsRepository.removeOldNews()
    }

    fun refresh(latest: DateTime?): LiveData<Boolean> {
        runTask {
            try {
                newsRepository.refreshNews(latest)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val message = handler.obtainMessage(LOADING_COMPLETE)
                message.sendToTarget()
            }
        }
        return loadingComplete
    }

    fun markNewsRead(readNews: List<Int>) {
        // TODO: 22.07.2019 send this list to client readNews
        println("read news: $readNews")
    }

    init {
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                loadingComplete.value = true
            }
        }
    }
}