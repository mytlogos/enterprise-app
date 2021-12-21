package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.ExternalMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.tools.DetailsLookup
import com.mytlogos.enterprise.tools.SimpleItemKeyProvider
import com.mytlogos.enterprise.tools.getDomain
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 * A fragment representing a List of DisplayReleases.
 */
@ExperimentalCoroutinesApi
class EpisodeFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BasePagingFragment<DisplayRelease, EpisodeViewModel>() {
    private lateinit var episodeViewModel: EpisodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        this.setTitle("Chapters")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override val viewModelClass: Class<EpisodeViewModel> = EpisodeViewModel::class.java

    @ExperimentalCoroutinesApi
    override fun createPaged(model: EpisodeViewModel) = model.displayEpisodes

    override fun createAdapter(): BaseAdapter<DisplayRelease, ViewHolder> {
        val releaseAdapter = ReleaseAdapter()
        releaseAdapter.holderInit = BaseAdapter.ViewInit { holder: ViewHolder ->
            holder.optionsButtonView.setOnClickListener {
                releaseAdapter.getItemFrom(holder)?.let { this.openPopup(holder, it) }
            }
        }
        return releaseAdapter
    }

    private class ReleaseItemCallback : DiffUtil.ItemCallback<DisplayRelease>() {
        override fun areItemsTheSame(oldItem: DisplayRelease, newItem: DisplayRelease): Boolean {
            return oldItem.episodeId == newItem.episodeId && oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: DisplayRelease, newItem: DisplayRelease): Boolean {
            return oldItem == newItem
        }
    }

    private class ReleaseAdapter :
        BaseAdapter<DisplayRelease, ViewHolder>(ReleaseItemCallback()) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            getItem(position)?.let { release ->
                // transform news id (int) to a string,
                // because it would expect a resource id if it is an int
                holder.metaView.text = release.releaseDate.toString("dd.MM.yyyy HH:mm:ss")
                holder.novelView.text = release.mediumTitle
                var title = release.title
                title = if (release.partialIndex > 0) {
                    "#${release.totalIndex}.${release.partialIndex} - $title"
                } else {
                    "#${release.totalIndex} - $title"
                }
                title = "$title (${getDomain(release.url)})"
                holder.contentView.text = title
            }
        }

        override val layoutId = R.layout.unreadchapter_item

        override fun createViewHolder(root: View, viewType: Int) = ViewHolder(root)
    }

    private class EpisodeFilterable(showToastFunction: ShowToast, val viewModel: EpisodeViewModel, val context: Context, val lifecycleOwner: LifecycleOwner) : Filterable {
        override val searchFilterProperties: Array<Property<*>> = arrayOf(
            IntTextProperty(
                R.id.minIndex,
                viewModel::minIndex,
                showToastFunction
            ),
            IntTextProperty(
                R.id.maxIndex,
                viewModel::maxIndex,
                showToastFunction
            ),
            SimpleTextProperty(R.id.host_filter, viewModel::host),
            SimplePositionProperty(R.id.read, viewModel::read),
            SimplePositionProperty(R.id.saved, viewModel::saved),
            SimpleProperty(R.id.latest_only, viewModel::isLatestOnly),
        )

        override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {
            val recycler: RecyclerView = view.findViewById(R.id.listsFilter) as RecyclerView
            val lists = viewModel.internLists
            val layoutManager = LinearLayoutManager(context)
            recycler.layoutManager = layoutManager

            val decoration = DividerItemDecoration(context, layoutManager.orientation)
            recycler.addItemDecoration(decoration)

            val adapter = ListAdapter()
            this.updateRecycler(adapter, lists.value)

            lists.observe(this.lifecycleOwner,
                { mediaLists: MutableList<MediaList> ->
                    updateRecycler(adapter, mediaLists)
                })

            recycler.adapter = adapter

            val selectionTracker = SelectionTracker.Builder(
                "MediaListSelection",
                recycler,
                SimpleItemKeyProvider(recycler),
                DetailsLookup(recycler),
                StorageStrategy.createLongStorage(),
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

            adapter.selectionTracker = selectionTracker
            selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val selectedIds = selectionTracker.selection.map { it.toInt() }
                    viewModel.filterListIds = selectedIds
                    adapter.sortItems(selectedIds)
                }
            })
        }

        fun updateRecycler(
            adapter: ListAdapter,
            mediaLists: MutableList<MediaList>?,
        ) {
            println("List: $mediaLists")

            if (mediaLists == null) {
                return
            }

            this.sortFlexibleList(adapter, mediaLists)
        }

        private fun sortFlexibleList(
            adapter: ListAdapter,
            flexibles: MutableList<MediaList>,
        ) {
            val listIds = viewModel.filterListIds

            flexibles.sortWith(Comparator { o1: MediaList, o2: MediaList ->
                val selected1 = listIds.contains(o1.listId)
                val selected2 = listIds.contains(o2.listId)

                return@Comparator if (selected1 == selected2) {
                    o1.name.compareTo(o2.name)
                } else {
                    if (selected1) -1 else 1
                }
            })

            adapter.setItems(flexibles)
        }

        override val filterLayout: Int
            get() = R.layout.filter_unread_episode_layout

        private class ListAdapter : RecyclerView.Adapter<TextOnlyViewHolder>(), ItemPositionable<MediaList> {
            private var items: MutableList<MediaList> = ArrayList()
            lateinit var selectionTracker: SelectionTracker<Long>

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextOnlyViewHolder {
                val root = LayoutInflater.from(parent.context).inflate(
                    R.layout.text_only_item,
                    parent,
                    false
                )
                val holder =  TextOnlyViewHolder(root)
                root.setOnClickListener {
                    val position = holder.bindingAdapterPosition

                    if (position != RecyclerView.NO_POSITION) {
                        val item = this.getItemAt(position)

                        if (item != null) {
                            selectionTracker.select(item.getSelectionKey())
                        } else {
                            println("EpisodeFragment: Trying to select a null MediaList item!")
                        }
                    }
                }
                return holder
            }

            @SuppressLint("NotifyDataSetChanged")
            fun setItems(items: MutableList<MediaList>) {
                this.items = items
                this.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            fun sortItems(selectedIds: List<Int>) {
                this.items.sortWith(Comparator { o1: MediaList, o2: MediaList ->
                    val selected1 = selectedIds.contains(o1.listId)
                    val selected2 = selectedIds.contains(o2.listId)

                    return@Comparator if (selected1 == selected2) {
                        o1.name.compareTo(o2.name)
                    } else {
                        if (selected1) -1 else 1
                    }
                })
                this.notifyDataSetChanged()
            }

            override fun onBindViewHolder(holder: TextOnlyViewHolder, position: Int) {
                getItemAt(position)?.let {
                    val listSource = if (it is ExternalMediaList) "External" else "Internal"
                    val title = "${it.name} ($listSource)"
                    holder.textView.text = title
                }
            }

            override fun getItemCount(): Int {
                return this.items.size
            }

            override fun getItemAt(position: Int): MediaList? {
                if (position < 0 || this.items.size <= position) return null
                return this.items[position]
            }

        }

        private class TextOnlyViewHolder(
            view: View,
        ) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view as TextView

            init {
                val params = textView.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 0, 0, 0)
                textView.textSize = 15f
                textView.requestLayout()
            }
        }
    }



    override fun createFilterable(): Filterable {
        return EpisodeFilterable(this::showToast, viewModel, requireContext(), this.viewLifecycleOwner)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.unread_chapter_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.group_by_medium_first) {
            item.isChecked = !item.isChecked
            episodeViewModel.grouped = item.isChecked
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(position: Int, item: DisplayRelease?) {
        if (item == null) {
            return
        }
        val mediumId = item.mediumId
        val fragment: TocFragment = TocFragment.newInstance(mediumId)
        mainActivity.switchWindow(fragment)
    }

    private fun openPopup(holder: ViewHolder, episode: DisplayRelease) {
        val popupMenu = PopupMenu(requireContext(), holder.optionsButtonView)
        if (episode.saved) {
            popupMenu
                .menu
                .add("Open Local")
                .setOnMenuItemClickListener {
                    lifecycleScope.launch {
                        val mediumType = instance.getMediumType(episode.mediumId)
                        openLocal(episode.episodeId, episode.mediumId, mediumType)
                    }
                    true
                }
        }
        popupMenu
            .menu
            .add("Open in Browser")
            .setOnMenuItemClickListener {
                this.openInBrowser(episode.url)
                true
            }
        if (!episode.read) {
            popupMenu
                .menu
                .add("Mark read")
                .setOnMenuItemClickListener {
                    instance.updateProgress(episode.episodeId, 1.0f)
                    true
                }
        }
        popupMenu.show()
    }

    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val contentView: TextView = mView.findViewById(R.id.content) as TextView
        val metaView: TextView = mView.findViewById(R.id.item_top_left) as TextView
        val novelView: TextView = mView.findViewById(R.id.item_top_right) as TextView
        val optionsButtonView: ImageButton = mView.findViewById(R.id.item_options_button) as ImageButton

        override fun toString(): String {
            return "${super.toString()} '${contentView.text}'"
        }
    }
}