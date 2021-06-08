package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ReadEpisode
import com.mytlogos.enterprise.model.Release
import com.mytlogos.enterprise.viewmodel.ReadEpisodeViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

/**
 * A fragment representing a list of Items.
 */
class ReadHistoryFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BaseListFragment<ReadEpisode, ReadEpisodeViewModel>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Read History")
        return view
    }

    override val viewModelClass: Class<ReadEpisodeViewModel>
        get() = ReadEpisodeViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<ReadEpisode>> {
        return viewModel.getReadEpisodes()
    }

    override fun createFlexible(value: ReadEpisode): IFlexible<*> {
        return ReadEpisodeItem(value)
    }

    private class SectionableReadEpisodeItem(val displayReadEpisode: ReadEpisode) :
        AbstractSectionableItem<ViewHolder, HeaderItem?>(
            HeaderItem(
                displayReadEpisode
                    .releases
                    .stream()
                    .max(Comparator.comparingInt { value: Release -> value.title!!.length })
                    .map(Release::title)
                    .orElse("Not available")!!,
                displayReadEpisode.mediumId)
        ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SectionableReadEpisodeItem) return false
            return displayReadEpisode.episodeId == other.displayReadEpisode.episodeId
        }

        override fun hashCode(): Int {
            return displayReadEpisode.episodeId
        }

        override fun getLayoutRes(): Int {
            return R.layout.unreadchapter_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>>,
        ): ViewHolder {
            return ViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.novelView.text = displayReadEpisode.mediumTitle
            holder.contentView.text = displayReadEpisode
                .releases
                .stream()
                .max(Comparator.comparingInt { value: Release -> value.title!!.length })
                .map(Release::title)
                .orElse("Not available")
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }

    private class ReadEpisodeItem(private val displayReadEpisode: ReadEpisode) :
        AbstractFlexibleItem<ViewHolder>() {

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SectionableReadEpisodeItem) return false
            return displayReadEpisode.episodeId == other.displayReadEpisode.episodeId
        }

        override fun hashCode(): Int {
            return displayReadEpisode.episodeId
        }

        override fun getLayoutRes(): Int {
            return R.layout.unreadchapter_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>>,
        ): ViewHolder {
            return ViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            val episode = displayReadEpisode

            holder.novelView.text = episode.mediumTitle
            var title = episode
                .releases
                .stream()
                .max(Comparator.comparingInt { value: Release -> value.title!!.length })
                .map(Release::title)
                .orElse("Not available")

            val partialIndex = episode.partialIndex
            val totalIndex = episode.totalIndex

            title = if (partialIndex > 0) {
                "#$totalIndex.$partialIndex - $title"
            } else {
                "#$totalIndex - $title"
            }
            holder.contentView.text = title
        }
    }

    private class HeaderItem(
        private val title: String,
        private val mediumId: Int,
    ) :
        AbstractHeaderItem<HeaderViewHolder>() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HeaderItem) return false
            return mediumId == other.mediumId
        }

        override fun hashCode(): Int {
            return mediumId
        }

        override fun getLayoutRes(): Int {
            return R.layout.flexible_header
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>>,
        ): HeaderViewHolder {
            return HeaderViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: HeaderViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            holder.textView.text = title
        }
    }

    private class HeaderViewHolder(
        itemView: View,
        adapter: FlexibleAdapter<IFlexible<*>>,
    ) : FlexibleViewHolder(itemView, adapter, true) {

        val textView: TextView = itemView.findViewById(R.id.text) as TextView
    }

    private class ViewHolder(mView: View, adapter: FlexibleAdapter<*>) :
        FlexibleViewHolder(mView, adapter) {

        val contentView: TextView = mView.findViewById(R.id.content) as TextView
        val novelView: TextView = mView.findViewById(R.id.item_top_right) as TextView

        override fun toString(): String {
            return "${super.toString()} '${contentView.text}'"
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface ReadChapterClickListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Any?)
    }
}