package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.Utils.getDomain
import com.mytlogos.enterprise.viewmodel.MediaInWaitListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.*

class MediaInWaitListFragment : BaseSwipePagingFragment<MediumInWait, MediaInWaitListViewModel>() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
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
                            return viewModel.titleFilter
                        }

                        override fun set(newFilter: String) {
                            viewModel.titleFilter = newFilter
                        }
                    },
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.host_filter
                        override val clearViewId: Int
                            get() = R.id.clear_host

                        override fun get(): String {
                            return viewModel.hostFilter
                        }

                        override fun set(newFilter: String) {
                            viewModel.hostFilter = newFilter
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

    override fun onSwipeRefresh() {
        LoadingTask().execute()
    }

    override fun onItemClick(position: Int, item: MediumInWait?) {
        if (item == null) {
            return
        }
        mainActivity.switchWindow(MediaInWaitFragment.getInstance(item))
        return
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LoadingTask : AsyncTask<Void, Void, Void>() {
        private var errorMsg: String? = null
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                viewModel.loadMediaInWait()
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
            (listContainer as SwipeRefreshLayout).isRefreshing = false
        }
    }

    override fun createAdapter(): BaseAdapter<MediumInWait, *> {
        return MediumInWaitAdapter()
    }

    @ExperimentalCoroutinesApi
    override fun createPaged(model: MediaInWaitListViewModel): Flow<PagingData<MediumInWait>> {
        return model.mediaInWait
    }

    class MediumInWaitHelper : DiffUtil.ItemCallback<MediumInWait>() {
        override fun areItemsTheSame(oldItem: MediumInWait, newItem: MediumInWait): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MediumInWait, newItem: MediumInWait): Boolean {
            return oldItem == newItem
        }
    }

    private class MediumInWaitAdapter :
        BaseAdapter<MediumInWait, NewMetaViewHolder>(MediumInWaitHelper()) {

        override val layoutId: Int
            get() = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)

        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            getItem(position)?.let {
                val mediumType: String = when (it.medium) {
                    MediumType.AUDIO -> "Audio"
                    MediumType.IMAGE -> "Bild"
                    MediumType.TEXT -> "Text"
                    MediumType.VIDEO -> "Video"
                    else -> throw IllegalStateException("no valid medium type: ${it.medium}")
                }
                val domain = getDomain(it.link)

                holder.topLeftText.text = mediumType
                holder.topRightText.text = domain
                holder.mainText.text = it.title
            }
        }
    }
}