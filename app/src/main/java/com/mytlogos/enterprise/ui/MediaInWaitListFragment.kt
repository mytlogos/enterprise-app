package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.Utils.getDomain
import com.mytlogos.enterprise.viewmodel.MediaInWaitListViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import java.io.IOException
import java.io.Serializable
import java.util.*

class MediaInWaitListFragment : BaseSwipeListFragment<MediumInWait, MediaInWaitListViewModel>() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Unused Media")
        return view
    }

    override fun createFilterable(): Filterable {
        return object : Filterable {
            override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {}
            override val filterLayout: Int
                get() = R.layout.filter_medium_in_wait_layout
            override val searchFilterProperties: Array<Property<*>>
                get() = arrayOf(
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.title_filter
                        override val clearViewId: Int
                            get() = R.id.clear_title

                        override fun get(): String {
                            return viewModel!!.titleFilter
                        }

                        override fun set(newFilter: String) {
                            viewModel!!.titleFilter = newFilter
                        }
                    },
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.host_filter
                        override val clearViewId: Int
                            get() = R.id.clear_host

                        override fun get(): String {
                            return viewModel!!.hostFilter
                        }

                        override fun set(newFilter: String) {
                            viewModel!!.hostFilter = newFilter
                        }
                    }
                )
        }
    }

    override val sortMap: LinkedHashMap<String, Sortings>
        get() {
            val map = LinkedHashMap<String, Sortings>()
            map["Title A-Z"] = Sortings.TITLE_AZ
            map["Title Z-A"] = Sortings.TITLE_ZA
            map["Medium Asc"] = Sortings.MEDIUM
            map["Medium Desc"] = Sortings.MEDIUM_REVERSE
            map["Host A-Z"] = Sortings.HOST_AZ
            map["Host Z-A"] = Sortings.HOST_ZA
            return map
        }
    override val viewModelClass: Class<MediaInWaitListViewModel>
        get() = MediaInWaitListViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<MediumInWait>> {
        return viewModel!!.mediaInWait!!
    }

    override fun createFlexible(value: MediumInWait): IFlexible<*> {
        return MediumItem(value)
    }

    override fun onSwipeRefresh() {
        LoadingTask().execute()
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        val item = flexibleAdapter!!.getItem(position) as MediumItem?
            ?: return false
        mainActivity.switchWindow(MediaInWaitFragment.getInstance(item.item))
        return true
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LoadingTask : AsyncTask<Void, Void, Void>() {
        private var errorMsg: String? = null
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                viewModel!!.loadMediaInWait()
            } catch (e: IOException) {
                errorMsg = "Loading went wrong"
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            val error = errorMsg
            if (error != null) {
                showToast(error)
            }
            (listContainer!! as SwipeRefreshLayout).isRefreshing = false
        }
    }

    private class MediumFilter private constructor(title: String?, val medium: Int, link: String?) :
        Serializable {
        val title: String = title?.lowercase(Locale.getDefault()) ?: ""
        val link: String = link?.lowercase(Locale.getDefault()) ?: ""

    }

    private class MediumItem(item: MediumInWait) :
        AbstractFlexibleItem<MetaViewHolder>() /*implements IFilterable<MediumFilter>*/ {
        val item: MediumInWait?
        override fun getLayoutRes(): Int {
            return R.layout.meta_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val mediumType: String
            mediumType = when (item!!.medium) {
                MediumType.AUDIO -> "Audio"
                MediumType.IMAGE -> "Bild"
                MediumType.TEXT -> "Text"
                MediumType.VIDEO -> "Video"
                else -> {
                    val msg = String.format("no valid medium type: %d", item.medium)
                    throw IllegalStateException(msg)
                }
            }
            holder.topLeftText.text = mediumType
            val domain = getDomain(item.link)
            holder.topRightText.text = domain
            holder.mainText.text = item.title
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as MediumItem
            return item == that.item
        }

        override fun hashCode(): Int {
            return item?.hashCode() ?: 0
        }

        fun filter(constraint: MediumFilter?): Boolean {
            if (constraint == null) {
                return true
            }
            if (constraint.medium > 0 && item!!.medium and constraint.medium == 0) {
                return false
            }
            return if (!constraint.link.isEmpty() && !item!!.link.lowercase(Locale.getDefault())
                    .contains(constraint.link)
            ) {
                false
            } else constraint.title.isEmpty() || item!!.title.lowercase(Locale.getDefault())
                .contains(constraint.title)
        }

        init {
            this.item = item
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }
}