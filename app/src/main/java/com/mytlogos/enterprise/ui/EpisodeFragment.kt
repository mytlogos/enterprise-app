package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.background.TaskManager.Companion.runCompletableTask
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Utils.getDomain
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

/**
 * A fragment representing a list of Items.
 */
class EpisodeFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BaseListFragment<DisplayRelease, EpisodeViewModel>() {
    private var groupByMedium = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Chapters")
        return view
    }

    override val viewModelClass: Class<EpisodeViewModel>
        get() = EpisodeViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<DisplayRelease>> {
        return viewModel!!.displayEpisodes
    }

    override fun createFilterable(): Filterable {
        return object : Filterable {
            override val searchFilterProperties: Array<Property<*>>
                get() = arrayOf(
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.minIndex

                        override fun get(): String {
                            return viewModel!!.minIndex.toString() + ""
                        }

                        override fun set(newFilter: String) {
                            try {
                                val index = newFilter.toInt()
                                viewModel!!.minIndex = index
                            } catch (e: NumberFormatException) {
                                showToast("Invalid Input")
                            }
                        }
                    },
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.maxIndex

                        override fun get(): String {
                            return viewModel!!.maxIndex.toString() + ""
                        }

                        override fun set(newFilter: String) {
                            try {
                                val index = newFilter.toInt()
                                viewModel!!.maxIndex = index
                            } catch (e: NumberFormatException) {
                                showToast("Invalid Input")
                            }
                        }
                    },
                    object : TextProperty {
                        override val viewId: Int
                            get() = R.id.host_filter

                        override fun get(): String {
                            return viewModel!!.host
                        }

                        override fun set(newFilter: String) {
                            viewModel!!.host = newFilter.lowercase(Locale.getDefault())
                        }
                    },
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.read

                        override fun get(): Int {
                            return viewModel!!.read
                        }

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun set(newFilter: Int) {
                            viewModel!!.read = newFilter
                        }
                    },
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.saved

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun get(): Int {
                            return viewModel!!.saved
                        }

                        override fun set(newFilter: Int) {
                            viewModel!!.saved = newFilter
                        }
                    },
                    object : BooleanProperty {
                        override val viewId: Int
                            get() = R.id.latest_only

                        override fun get(): Boolean {
                            return viewModel!!.isLatestOnly
                        }

                        override fun set(newFilter: Boolean) {
                            viewModel!!.isLatestOnly = newFilter
                        }
                    }
                )

            override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {
                val recycler: RecyclerView = view.findViewById(R.id.listsFilter)
                val lists = viewModel!!.lists
                val layoutManager = LinearLayoutManager(requireContext())
                recycler.layoutManager = layoutManager
                val decoration = DividerItemDecoration(requireContext(), layoutManager.orientation)
                recycler.addItemDecoration(decoration)
                val flexibleAdapter = FlexibleAdapter<IFlexible<*>>(null)
                updateRecycler(flexibleAdapter, lists.value)
                lists.observe(this@EpisodeFragment,
                    { mediaLists: MutableList<MediaList> -> updateRecycler(flexibleAdapter, mediaLists) })
                flexibleAdapter.mode = SelectableAdapter.Mode.MULTI
                flexibleAdapter.addListener(FlexibleAdapter.OnItemClickListener { _: View?, position: Int ->
                    flexibleAdapter.toggleSelection(position)
                    val listIds: MutableList<Int> = ArrayList()
                    for (selectedPosition in flexibleAdapter.selectedPositions) {
                        val item = flexibleAdapter.getItem(selectedPosition!!,
                            FlexibleListItem::class.java)
                        listIds.add(Objects.requireNonNull(item)!!.mediaList.listId)
                    }
                    viewModel!!.filterListIds = listIds
                    sortFlexibleList(flexibleAdapter)
                    true
                })
                recycler.adapter = flexibleAdapter
            }

            fun updateRecycler(
                flexibleAdapter: FlexibleAdapter<IFlexible<*>>,
                mediaLists: MutableList<MediaList>?
            ) {
                println("List: $mediaLists")
                if (mediaLists == null) {
                    return
                }
                val shouldListIds = viewModel!!.filterListIds
                mediaLists.sortWith(Comparator { o1: MediaList, o2: MediaList ->
                    val selected1 = shouldListIds.contains(o1.listId)
                    val selected2 = shouldListIds.contains(o2.listId)
                    if (selected1 == selected2) {
                        return@Comparator o1.name.compareTo(o2.name)
                    } else {
                        return@Comparator if (selected1) -1 else 1
                    }
                })
                val flexibles: MutableList<IFlexible<*>> = ArrayList(mediaLists.size)
                for (mediaList in mediaLists) {
                    flexibles.add(FlexibleListItem(mediaList))
                }
                flexibleAdapter.updateDataSet(flexibles)
                val currentItems = flexibleAdapter.currentItems
                var i = 0
                val currentItemsSize = currentItems.size
                while (i < currentItemsSize) {
                    val item = currentItems[i] as FlexibleListItem
                    if (shouldListIds.contains(item.mediaList.listId)) {
                        flexibleAdapter.addSelection(i)
                    } else {
                        flexibleAdapter.removeSelection(i)
                    }
                    i++
                }
            }

            private fun sortFlexibleList(flexibleAdapter: FlexibleAdapter<IFlexible<*>>) {
                val list: MutableList<IFlexible<*>> = ArrayList(flexibleAdapter.currentItems)
                val listIds = viewModel!!.filterListIds
                list.sortWith(Comparator { o1: IFlexible<*>, o2: IFlexible<*> ->
                    val item1 = o1 as FlexibleListItem
                    val item2 = o2 as FlexibleListItem
                    val selected1 = listIds.contains(item1.mediaList.listId)
                    val selected2 = listIds.contains(item2.mediaList.listId)
                    if (selected1 == selected2) {
                        return@Comparator item1.mediaList.name.compareTo(item2.mediaList.name)
                    } else {
                        return@Comparator if (selected1) -1 else 1
                    }
                })
                flexibleAdapter.updateDataSet(list)
                var i = 0
                val currentItemsSize = list.size
                while (i < currentItemsSize) {
                    val item = list[i] as FlexibleListItem
                    if (listIds.contains(item.mediaList.listId)) {
                        flexibleAdapter.addSelection(i)
                    } else {
                        flexibleAdapter.removeSelection(i)
                    }
                    i++
                }
            }

            override val filterLayout: Int
                get() = R.layout.filter_unread_episode_layout
        }
    }

    private class FlexibleListItem(val mediaList: MediaList) :
        AbstractFlexibleItem<TextOnlyViewHolder>() {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as FlexibleListItem
            return mediaList == that.mediaList
        }

        override fun hashCode(): Int {
            return mediaList.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.text_only_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): TextOnlyViewHolder {
            return TextOnlyViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: TextOnlyViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val listSource = if (mediaList is ExternalMediaList) "External" else "Internal"
            val title = String.format("%s (%s)", mediaList.name, listSource)
            holder.textView.text = title
            val drawable = DrawableUtils.getSelectableBackgroundCompat(
                Color.WHITE,  // normal background
                Color.GRAY,  // pressed background
                Color.BLACK) // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable)
        }
    }

    private class TextOnlyViewHolder(
        view: View,
        adapter: FlexibleAdapter<*>?
    ) : FlexibleViewHolder(view, adapter) {
        val textView: TextView = view as TextView

        init {
            val params = textView.layoutParams as MarginLayoutParams
            params.setMargins(0, 0, 0, 0)
            textView.textSize = 15f
            textView.requestLayout()
        }
    }

    override fun createFlexible(value: DisplayRelease): IFlexible<*> {
        return if (groupByMedium) {
            SectionableEpisodeItem(value, this)
        } else {
            EpisodeItem(value, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.unread_chapter_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.group_by_medium) {
            toggleGroupByMedium(item)
        } else if (item.itemId == R.id.group_by_medium_first) {
            item.isChecked = !item.isChecked
            viewModel!!.setGrouped(item.isChecked)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        val mediumId: Int = when (val item = flexibleAdapter!!.getItem(position)) {
            is EpisodeItem -> {
                item.episode.mediumId
            }
            is SectionableEpisodeItem -> {
                item.episode.mediumId
            }
            else -> {
                return false
            }
        }
        val fragment: TocFragment = TocFragment.newInstance(mediumId)
        mainActivity.switchWindow(fragment)
        return true
    }

    private fun toggleGroupByMedium(item: MenuItem) {
        item.isChecked = !item.isChecked
        groupByMedium = item.isChecked
        val list = livePagedList!!.value ?: return
        val flexibles = convertToFlexible(list)
        flexibleAdapter!!.updateDataSet(flexibles)
    }

    private fun openPopup(holder: ViewHolder, episode: DisplayRelease) {
        val popupMenu = PopupMenu(context, holder.optionsButtonView)
        if (episode.saved) {
            popupMenu
                .menu
                .add("Open Local")
                .setOnMenuItemClickListener {
                    val task = runCompletableTask { instance.getMediumType(episode.mediumId) }
                    task.whenComplete { type: Int?, _: Throwable? ->
                        if (type != null) {
                            openLocal(episode.episodeId, episode.mediumId, type)
                        }
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
        popupMenu.show()
    }

    private class SectionableEpisodeItem(
        val episode: DisplayRelease,
        private val fragment: EpisodeFragment
    ) : AbstractSectionableItem<ViewHolder, HeaderItem?>(
        HeaderItem(
            episode.mediumTitle, episode.mediumId)) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o !is SectionableEpisodeItem) return false
            return episode == o.episode
        }

        override fun hashCode(): Int {
            return episode.hashCode()
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
            holder.metaView.text = episode.releaseDate.toString("dd.MM.yyyy HH:mm:ss")
            holder.novelView.text = episode.mediumTitle
            holder.contentView.text = episode.title
            holder.optionsButtonView.setOnClickListener {
                fragment.openPopup(holder,
                    episode)
            }
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

    private class EpisodeItem(
        val episode: DisplayRelease,
        private val fragment: EpisodeFragment
    ) : AbstractFlexibleItem<ViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SectionableEpisodeItem) return false
            return episode.episodeId == other.episode.episodeId
        }

        override fun hashCode(): Int {
            return episode.episodeId
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
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.text = episode.releaseDate.toString("dd.MM.yyyy HH:mm:ss")
            holder.novelView.text = episode.mediumTitle
            var title = episode.title
            title = if (episode.partialIndex > 0) {
                String.format("#%d.%d - %s", episode.totalIndex, episode.partialIndex, title)
            } else {
                String.format("#%d - %s", episode.totalIndex, title)
            }
            title = String.format("%s (%s) ", title, getDomain(
                episode.url))
            holder.contentView.text = title
            holder.optionsButtonView.setOnClickListener {
                fragment.openPopup(holder,
                    episode)
            }
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }

    private class HeaderViewHolder(
        itemView: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ) : FlexibleViewHolder(itemView, adapter, true) {
        val textView: TextView = itemView.findViewById(R.id.text)
    }

    private class ViewHolder(mView: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(mView, adapter) {
        val contentView: TextView = mView.findViewById(R.id.content)
        val metaView: TextView = mView.findViewById(R.id.item_top_left)
        val novelView: TextView = mView.findViewById(R.id.item_top_right)
        val optionsButtonView: ImageButton = mView.findViewById(R.id.item_options_button)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}