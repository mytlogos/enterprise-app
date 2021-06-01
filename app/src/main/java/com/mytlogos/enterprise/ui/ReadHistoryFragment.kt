package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
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
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Read History")
        return view
    }

    override val viewModelClass: Class<ReadEpisodeViewModel>
        get() = ReadEpisodeViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<ReadEpisode>> {
        return viewModel!!.getReadEpisodes()
    }

    override fun createFlexible(value: ReadEpisode): IFlexible<*> {
        return ReadEpisodeItem(value, this)
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
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): ViewHolder {
            return ViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            holder.mItem = displayReadEpisode
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

    private class ReadEpisodeItem(
        private val displayReadEpisode: ReadEpisode,
        fragment: Fragment?
    ) : AbstractFlexibleItem<ViewHolder>() {
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
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): ViewHolder {
            return ViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val episode = displayReadEpisode
            holder.mItem = episode
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.novelView.text = episode.mediumTitle
            var title = displayReadEpisode
                .releases
                .stream()
                .max(Comparator.comparingInt { value: Release -> value.title!!.length })
                .map(Release::title)
                .orElse("Not available")
            title = if (episode.partialIndex > 0) {
                String.format("#%d.%d - %s", episode.totalIndex, episode.partialIndex, title)
            } else {
                String.format("#%d - %s", episode.totalIndex, title)
            }
            holder.contentView.text = title
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }

    private class HeaderItem(private val title: String, private val mediumId: Int) :
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
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): HeaderViewHolder {
            return HeaderViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: HeaderViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            holder.textView.text = title
        }
    }

    private class HeaderViewHolder(
        itemView: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ) : FlexibleViewHolder(itemView, adapter, true) {
        val textView: TextView

        init {
            textView = itemView.findViewById(R.id.text)
        }
    }

    private class ViewHolder(mView: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(
            mView, adapter) {
        val contentView: TextView
        private val metaView: TextView
        val novelView: TextView
        private val optionsButtonView: ImageButton
        var mItem: ReadEpisode? = null
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        init {
            metaView = mView.findViewById(R.id.item_top_left)
            novelView = mView.findViewById(R.id.item_top_right)
            contentView = mView.findViewById(R.id.content)
            optionsButtonView = mView.findViewById(R.id.item_options_button)
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