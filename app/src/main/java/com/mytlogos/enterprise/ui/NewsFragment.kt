package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.News
import com.mytlogos.enterprise.tools.Utils.getDomain
import com.mytlogos.enterprise.ui.NewsFragment.AttachedListener
import com.mytlogos.enterprise.viewmodel.NewsViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.LayoutUtils
import eu.davidea.viewholders.FlexibleViewHolder
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A fragment representing a list of Items.
 *
 *
 */
class NewsFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BaseSwipeListFragment<News, NewsViewModel>() {
    private var attachedListener: AttachedListener? = null
    private lateinit var attachedFuture: ScheduledFuture<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.list) as RecyclerView
        val service = Executors.newSingleThreadScheduledExecutor()
        val attachedTask: Runnable = object : Runnable {
            override fun run() {
                if (attachedListener == null) {
                    attachedFuture = service.schedule(this, 2, TimeUnit.SECONDS)
                    return
                }
                attachedListener!!.handle()
            }
        }
        attachedFuture = service.schedule(attachedTask, 2, TimeUnit.SECONDS)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                attachedFuture.cancel(true)

                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    attachedFuture = service.schedule(attachedTask, 2, TimeUnit.SECONDS)
                }
            }
        })

        // FIXME: 22.07.2019 does not work quite correctly:
        //  -when one does not scroll
        //  -one switches to one news page a second time
        //  it does not get the correct newsItems
        attachedListener = AttachedListener {
            val firstPosition = LayoutUtils.findFirstCompletelyVisibleItemPosition(recyclerView)
            val lastPosition = LayoutUtils.findLastCompletelyVisibleItemPosition(recyclerView)

            if (firstPosition == RecyclerView.NO_POSITION || lastPosition == RecyclerView.NO_POSITION) {
                return@AttachedListener
            }
            val minimumVisible = DateTime.now().minusSeconds(2)
            val readNews: MutableList<Int> = ArrayList()

            for (i in firstPosition..lastPosition) {
                val item = flexibleAdapter.getItem(i)

                if (item !is NewsItem || item.attached == null) {
                    continue
                }

                if (item.attached!!.isBefore(minimumVisible) && !item.news.read) {
                    readNews.add(item.news.id)
                }
            }

            if (readNews.isNotEmpty()) {
                viewModel.markNewsRead(readNews)
            }
        }
        this.setTitle("News")
        return view
    }

    override val viewModelClass: Class<NewsViewModel>
        get() = NewsViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<News>> {
        return viewModel.news
    }

    override fun createFlexible(value: News): IFlexible<*> {
        val item = NewsItem(value)
        item.listener = attachedListener
        return item
    }

    override fun onSwipeRefresh() {
        val news: List<News?>? = livePagedList.value
        var latest: DateTime? = null

        if (news != null) {
            val newsComparator = Comparator.nullsLast { o1: News, o2: News ->
                o1.getTimeStamp().compareTo(o2.getTimeStamp())
            }
            val latestNews = Collections.max(news, newsComparator)
            latest = latestNews?.getTimeStamp()
        }
        viewModel.refresh(latest).observe(this, { loadingComplete: Boolean? ->
            if (loadingComplete != null && loadingComplete) {
                (listContainer as SwipeRefreshLayout).isRefreshing = false
            }
        })
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        val item = flexibleAdapter.getItem(position) as? NewsItem ?: return false
        val url = item.news.url
        this.openInBrowser(url)
        return false
    }

    private fun interface AttachedListener {
        fun handle()
    }

    private class HeaderItem(private val date: LocalDate) : AbstractHeaderItem<HeaderViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HeaderItem) return false
            return date == other.date
        }

        override fun hashCode(): Int {
            return date.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.flexible_header
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?,
        ): HeaderViewHolder {
            return HeaderViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: HeaderViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            holder.textView.text = date.toString("E, dd.MM.yyyy")
        }
    }

    private class HeaderViewHolder(
        itemView: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?,
    ) : FlexibleViewHolder(itemView, adapter, true) {
        val textView: TextView = itemView.findViewById(R.id.text)
    }

    private class NewsItem(val news: News) :
        AbstractSectionableItem<MetaViewHolder, HeaderItem?>(
            HeaderItem(news.getTimeStamp().toLocalDate())
        ) {

        var attached: DateTime? = null
        var listener: AttachedListener? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NewsItem) return false
            return news.id == other.news.id
        }

        override fun hashCode(): Int {
            return news.id
        }

        override fun getLayoutRes(): Int {
            return R.layout.meta_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>>,
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.topLeftText.text = news.getTimeStamp().toString("HH:mm:ss")
            holder.mainText.text = news.getTitle()
            holder.topRightText.text = getDomain(news.url)
        }

        override fun onViewAttached(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
        ) {
            super.onViewAttached(adapter, holder, position)
            attached = DateTime.now()
        }

        override fun onViewDetached(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
        ) {
            super.onViewDetached(adapter, holder, position)
            attached = null
            listener?.handle()
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }
}