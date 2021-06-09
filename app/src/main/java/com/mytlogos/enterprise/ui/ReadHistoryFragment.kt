package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ReadEpisode
import com.mytlogos.enterprise.model.Release
import com.mytlogos.enterprise.viewmodel.ReadEpisodeViewModel
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * A fragment representing a list of Items.
 */
class ReadHistoryFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BasePagingFragment<ReadEpisode, ReadEpisodeViewModel>() {

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

    override fun createAdapter(): BaseAdapter<ReadEpisode, *> = ReadEpisodeAdapter()

    override fun createPaged(model: ReadEpisodeViewModel): Flow<PagingData<ReadEpisode>> {
        return viewModel.getReadEpisodes()
    }

    private class UserDiff : DiffUtil.ItemCallback<ReadEpisode>() {
        override fun areItemsTheSame(oldItem: ReadEpisode, newItem: ReadEpisode): Boolean {
            return oldItem.episodeId == newItem.episodeId
        }

        override fun areContentsTheSame(oldItem: ReadEpisode, newItem: ReadEpisode): Boolean {
            return oldItem == newItem
        }
    }

    private class ReadEpisodeAdapter : BaseAdapter<ReadEpisode, ViewHolder>(UserDiff()) {

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                holder.novelView.text = item.mediumTitle
                var title = item
                    .releases
                    .stream()
                    .max(Comparator.comparingInt { value: Release -> value.title!!.length })
                    .map(Release::title)
                    .orElse("Not available")

                val partialIndex = item.partialIndex
                val totalIndex = item.totalIndex

                title = if (partialIndex > 0) {
                    "#$totalIndex.$partialIndex - $title"
                } else {
                    "#$totalIndex - $title"
                }
                holder.contentView.text = title
            }
        }

        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = ViewHolder(root)
    }

    private class ViewHolder(mView: View) :
        RecyclerView.ViewHolder(mView) {

        val contentView: TextView = mView.findViewById(R.id.content) as TextView
        val novelView: TextView = mView.findViewById(R.id.item_top_right) as TextView

        override fun toString(): String {
            return "${super.toString()} '${contentView.text}'"
        }

    }
}