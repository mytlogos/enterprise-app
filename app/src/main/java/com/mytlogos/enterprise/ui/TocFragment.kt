package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.background.TaskManager.Companion.runCompletableTask
import com.mytlogos.enterprise.model.Release
import com.mytlogos.enterprise.model.TocEpisode
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.TocEpisodeViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

/**
 * A fragment representing a list of Items.
 *
 *
 */
class TocFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BaseListFragment<TocEpisode, TocEpisodeViewModel>() {
    private var mediumId = 0
    private var inActionMode = false
    private var actionType: ActionType? = null
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        @SuppressLint("StaticFieldLeak")
        private var relativeLayout: RelativeLayout? = null
        private var navigationView: BottomNavigationView? = null
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "ToC Actions"
            Objects.requireNonNull(mainActivity.supportActionBar)!!.hide()
            navigationView = BottomNavigationView(requireContext())
            relativeLayout = view as RelativeLayout?
            if (relativeLayout == null) {
                System.err.println("root view is null in TocFragment for ActionMode")
                showToast("An Error occurred while entering ActionMode")
                return false
            }
            layoutInflater.inflate(R.layout.bottom_navigation, relativeLayout)
            mode.menuInflater.inflate(R.menu.toc_action_menu, menu)
            navigationView = relativeLayout!!.findViewById(R.id.navigation)
            navigationView!!.inflateMenu(R.menu.toc_menu)
            navigationView!!.itemIconTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),
                intArrayOf()), intArrayOf(
                requireContext().resources.getColor(R.color.colorPrimary, null),
                Color.WHITE
            ))
            navigationView!!.setOnNavigationItemSelectedListener { item: MenuItem ->
                actionType = when (item.itemId) {
                    R.id.mark_read -> ActionType.MARK_READ
                    R.id.mark_unread -> ActionType.MARK_UNREAD
                    R.id.download -> ActionType.DOWNLOAD
                    R.id.delete_local -> ActionType.DELETE_SAVED_EPISODE
                    R.id.refresh -> ActionType.RELOAD
                    else -> {
                        showToast("Unknown Selected Item")
                        return@setOnNavigationItemSelectedListener false
                    }
                }
                displayActionModeActions()
                true
            }
            flexibleAdapter!!.mode = SelectableAdapter.Mode.MULTI
            inActionMode = true
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val adapter = flexibleAdapter
            when (item.itemId) {
                R.id.clear -> {
                    adapter!!.clearSelection()
                    return true
                }
                R.id.select_between -> {
                    val positions = adapter!!.selectedPositions
                    var highest = -1
                    var lowest = Int.MAX_VALUE
                    if (positions.isEmpty()) {
                        return true
                    }
                    for (position in positions) {
                        highest = Math.max(position!!, highest)
                        lowest = Math.min(position, lowest)
                    }
                    if (highest < 0) {
                        System.err.println("A selected positions which are not positive")
                        return true
                    }
                    val items = adapter.currentItems
                    val holders: List<FlexibleViewHolder> = ArrayList(
                        adapter.allBoundViewHolders)
                    var i = lowest
                    while (i <= highest && i < items.size) {
                        if (!adapter.isSelected(i)) {
                            adapter.toggleSelection(i)
                            // hacky fix as adapter.toggleSelection alone
                            // does not show animation and state visibly (no change seen)
                            for (holder in holders) {
                                val position = holder.flexibleAdapterPosition
                                if (position == i) {
                                    holder.toggleActivation()
                                    break
                                }
                            }
                        }
                        i++
                    }
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Objects.requireNonNull(mainActivity.supportActionBar)!!.show()
            relativeLayout!!.removeView(navigationView)
            actionType = null
            flexibleAdapter!!.mode = SelectableAdapter.Mode.IDLE
            flexibleAdapter!!.clearSelection()
            inActionMode = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = requireArguments()
        mediumId = bundle.getInt(MEDIUM_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        registerForContextMenu(listView!!)
        // TODO: 22.07.2019 set the mediumTitle
        this.setTitle("Table of Contents")
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.medium_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_setting) {
            val mediumId = requireArguments().getInt(MEDIUM_ID)
            val fragment: MediumSettingFragment =
                MediumSettingFragment.newInstance(mediumId)
            this.mainActivity.switchWindow(fragment, true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getPosition(text: String): Int {
        var position = super.getPosition(text)
        val list = livePagedList
        val value = list!!.value
        if (value == null || position < 0) {
            return position
        }
        val sort = viewModel!!.getSort()
        if (sort === Sortings.INDEX_DESC) {
            position = value.size - position
        } else if (sort !== Sortings.INDEX_ASC) {
            showToast("Unknown Sort")
            position = -1
        }
        return position
    }

    override val viewModelClass: Class<TocEpisodeViewModel>
        get() = TocEpisodeViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<TocEpisode>> {
        return viewModel!!.getToc(mediumId)
    }

    override fun createFlexible(value: TocEpisode): IFlexible<*> {
        return ListItem(value)
    }

    override fun createFilterable(): Filterable {
        return object : Filterable {
            override val searchFilterProperties: Array<Property<*>>
                get() = arrayOf(
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.read

                        override fun get(): Int {
                            return viewModel!!.readFilter.toInt()
                        }

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun set(newFilter: Int) {
                            viewModel!!.readFilter = newFilter.toByte()
                        }
                    },
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.saved

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun get(): Int {
                            return viewModel!!.savedFilter.toInt()
                        }

                        override fun set(newFilter: Int) {
                            viewModel!!.savedFilter = newFilter.toByte()
                        }
                    })
            override val filterLayout: Int
                get() = R.layout.toc_filter
        }
    }

    override val sortMap: LinkedHashMap<String, Sortings>
        get() {
            val hashMap = LinkedHashMap<String, Sortings>()
            hashMap["Index Asc"] = Sortings.INDEX_ASC
            hashMap["Index Desc"] = Sortings.INDEX_DESC
            return hashMap
        }

    override fun onItemClick(view: View, position: Int): Boolean {
        if (inActionMode) {
            if (position != RecyclerView.NO_POSITION) {
                flexibleAdapter!!.toggleSelection(position)
                return true
            }
            return false
        }
        val flexible = flexibleAdapter!!.getItem(position) as? ListItem ?: return false
        val item = flexible
        if (item.item.isSaved) {
            val task = runCompletableTask {
                instance.getMediumType(
                    mediumId)
            }
            task.whenComplete { type: Int?, throwable: Throwable? ->
                if (type != null) {
                    openLocal(item.item.episodeId, mediumId, type)
                }
            }
        } else {
            val urls = item.item.releases.mapNotNull(Release::url)
            openInBrowser(urls)
        }
        return false
    }

    override fun onItemLongClick(position: Int) {
        if (!inActionMode) {
            this.mainActivity.startActionMode(callback)
            flexibleAdapter!!.addSelection(position)
        } else {
            if (position != RecyclerView.NO_POSITION) {
                flexibleAdapter!!.toggleSelection(position)
            }
        }
    }

    private fun displayActionModeActions() {
        if (actionType == null) {
            System.err.println("not action type selected")
            showToast("An Error occurred, cannot open Action Popup")
            return
        }
        val items: MutableList<TocEpisode> = ArrayList()
        for (position in flexibleAdapter!!.selectedPositions) {
            val item = flexibleAdapter!!.getItem(position)
            if (item is ListItem) {
                items.add(item.item)
            }
        }
        if (items.isEmpty()) {
            return
        }
        val firstItem = items[0]
        val actionCountMap: MutableMap<String?, ActionCount> = HashMap()
        for (value in ActionCount.values()) {
            actionCountMap[value.title] = value
        }
        val menuItems: MutableList<String> = ArrayList(actionCountMap.keys)
        var title: String = when (actionType) {
            ActionType.RELOAD -> "Refresh"
            ActionType.DOWNLOAD -> "Download"
            ActionType.MARK_READ -> "Mark read"
            ActionType.MARK_UNREAD -> "Mark unread"
            ActionType.DELETE_SAVED_EPISODE -> "Delete Saved"
            else -> {
                showToast("Unknown ActionType")
                return
            }
        }
        title += ":"
        if (items.size == 1) {
            when (actionType) {
                ActionType.DOWNLOAD -> if (firstItem.isSaved) {
                    menuItems.removeAt(0)
                }
                ActionType.MARK_READ -> if (firstItem.progress == 1f) {
                    menuItems.removeAt(0)
                }
                ActionType.MARK_UNREAD -> if (firstItem.progress < 1) {
                    menuItems.removeAt(0)
                }
                ActionType.DELETE_SAVED_EPISODE -> if (!firstItem.isSaved) {
                    menuItems.removeAt(0)
                }
            }
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(menuItems.toTypedArray()) { dialog: DialogInterface?, which: Int ->
                val menuTitle = menuItems[which]
                val count = actionCountMap[menuTitle]
                if (count == null) {
                    showToast("Unknown MenuItem")
                } else {
                    handle(items, actionType!!, count)
                }
            }
            .show()
    }

    fun handle(items: List<TocEpisode>, type: ActionType, count: ActionCount): Boolean {
        ChangeEpisodeReadStatus(type,
            items,
            mediumId,
            count,
            items,
            this.context,
            viewModel).execute()
        return true
    }

    private class ChangeEpisodeReadStatus(
        private val type: ActionType,
        selected: List<TocEpisode>,
        mediumId: Int,
        count: ActionCount,
        episodes: List<TocEpisode?>?,
        context: Context?,
        viewModel: TocEpisodeViewModel?
    ) : AsyncTask<Void, Void, Void>() {
        private val selected: List<TocEpisode>?
        private val mediumId: Int
        private val count: ActionCount
        private val episodes: List<TocEpisode?>?

        @SuppressLint("StaticFieldLeak")
        private val context: Context?
        private val viewModel: TocEpisodeViewModel?
        private var errorMessage: String? = null
        override fun doInBackground(vararg voids: Void?): Void? {
            if (mediumId <= 0 || selected == null || selected.isEmpty() || episodes == null) {
                return null
            }
            try {
                val episodeIds: MutableSet<Int> = HashSet()
                val indices: MutableList<Double> = ArrayList()
                for (tocEpisode in selected) {
                    episodeIds.add(tocEpisode.episodeId)
                    val combiIndex =
                        (tocEpisode.totalIndex.toString() + "." + tocEpisode.partialIndex).toDouble()
                    indices.add(combiIndex)
                }
                when (type) {
                    ActionType.MARK_READ -> viewModel!!.updateRead(episodeIds,
                        indices,
                        count,
                        mediumId,
                        true)
                    ActionType.MARK_UNREAD -> viewModel!!.updateRead(episodeIds,
                        indices,
                        count,
                        mediumId,
                        false)
                    ActionType.DELETE_SAVED_EPISODE -> viewModel!!.deleteLocalEpisode(episodeIds,
                        indices,
                        count,
                        mediumId)
                    ActionType.DOWNLOAD -> viewModel!!.download(episodeIds,
                        indices,
                        count,
                        mediumId)
                    ActionType.RELOAD -> viewModel!!.reload(episodeIds, indices, count, mediumId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Could not execute Action"
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        init {
            this.selected = selected
            this.mediumId = mediumId
            this.count = count
            this.episodes = episodes
            this.context = context
            this.viewModel = viewModel
        }
    }

    private class ListItem(val item: TocEpisode) :
        AbstractFlexibleItem<ViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val listItem = other as ListItem
            return item == listItem.item
        }

        override fun hashCode(): Int {
            return item.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.episode_item
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
            val index: String
            index = if (item.partialIndex > 0) {
                String.format("#%d.%d", item.totalIndex, item.partialIndex)
            } else {
                String.format("#%d", item.totalIndex)
            }
            val comparator =
                Comparator { o1: Release, o2: Release -> o1.releaseDate!!.compareTo(o2.releaseDate) }
            val earliestRelease = item.releases.stream().min(comparator)
            val release = earliestRelease.orElse(null)
            val topRight: String
            topRight = if (release == null) {
                "Not available"
            } else {
                release.releaseDate!!.toString("dd.MM.yyyy HH:mm:ss")
            }
            val title = item
                .releases
                .stream()
                .map(Release::title)
                .max { o1: String?, o2: String? -> (o1?.length ?: 0) - (o2?.length ?: 0) }
                .orElse("Not available")
            val hasOnline = item.releases.stream()
                .anyMatch { any: Release -> any.url != null && !any.url!!.isEmpty() }
            val isLocked = item.releases.stream().allMatch(Release::locked)
            holder.textTopLeft.text = index
            holder.textTopRight.text = topRight
            holder.textContentView.text = title
            holder.episodeLockedIcon.visibility = if (isLocked) View.VISIBLE else View.GONE
            holder.episodeReadIcon.alpha = if (item.progress == 1f) 1f else 0.25f
            holder.openLocalIcon.alpha = if (item.isSaved) 1f else 0.25f
            holder.openBrowserIcon.alpha = if (hasOnline) 1f else 0.25f
            val drawable = DrawableUtils.getSelectableBackgroundCompat(
                Color.WHITE,  // normal background
                Color.GRAY,  // pressed background
                Color.BLACK) // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable)
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = true
        }
    }

    private class ViewHolder(mView: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(
            mView, adapter) {
        val textContentView: TextView
        val textTopLeft: TextView
        val textTopRight: TextView
        val episodeLockedIcon: ImageView
        val episodeReadIcon: ImageView
        val openBrowserIcon: ImageView
        val openLocalIcon: ImageView
        override fun shouldAddSelectionInActionMode(): Boolean {
            return true
        }

        override fun toString(): String {
            return super.toString() + " '" + textContentView.text + "'"
        }

        init {
            textTopLeft = mView.findViewById(R.id.item_top_left)
            textTopRight = mView.findViewById(R.id.item_top_right)
            episodeLockedIcon = mView.findViewById(R.id.episode_locked)
            episodeReadIcon = mView.findViewById(R.id.episode_read)
            openBrowserIcon = mView.findViewById(R.id.open_in_browser)
            openLocalIcon = mView.findViewById(R.id.open_local)
            textContentView = mView.findViewById(R.id.content)
        }
    }

    enum class ActionType {
        MARK_READ, MARK_UNREAD, MARK_PREVIOUSLY_READ, MARK_PREVIOUSLY_UNREAD, MARK_ALL_READ, MARK_ALL_UNREAD, DELETE_SAVED_EPISODE, DELETE_PREVIOUSLY_SAVED_EPISODE, DELETE_ALL_SAVED_EPISODE, DOWNLOAD, RELOAD
    }

    companion object {
        private const val MEDIUM_ID = "mediumId"
        fun newInstance(mediumId: Int): TocFragment {
            val fragment = TocFragment()
            val args = Bundle()
            args.putInt(MEDIUM_ID, mediumId)
            fragment.arguments = args
            return fragment
        }
    }
}