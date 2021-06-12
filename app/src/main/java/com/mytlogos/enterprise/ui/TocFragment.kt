package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.background.TaskManager.Companion.runCompletableTask
import com.mytlogos.enterprise.model.ActionCount
import com.mytlogos.enterprise.model.Release
import com.mytlogos.enterprise.model.TocEpisode
import com.mytlogos.enterprise.requireSupportActionBar
import com.mytlogos.enterprise.tools.SelectableViewHolder
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.getKeyFrom
import com.mytlogos.enterprise.tools.getPositionFrom
import com.mytlogos.enterprise.viewmodel.TocEpisodeViewModel
import eu.davidea.flexibleadapter.utils.DrawableUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * A fragment representing the List of Releases of a single Medium.
 *
 *
 */
class TocFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BasePagingFragment<TocEpisode, TocEpisodeViewModel>() {
    private var mediumId = 0
    private var inActionMode = false
    private var actionType: ActionType? = null

    private val callback: ActionMode.Callback = object : ActionMode.Callback {

        @SuppressLint("StaticFieldLeak")
        private lateinit var relativeLayout: RelativeLayout
        private lateinit var navigationView: BottomNavigationView

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "ToC Actions"
            mainActivity.requireSupportActionBar().hide()
            navigationView = BottomNavigationView(requireContext())

            if (view == null) {
                System.err.println("root view is null in TocFragment for ActionMode")
                showToast("An Error occurred while entering ActionMode")
                return false
            }
            relativeLayout = view as RelativeLayout

            layoutInflater.inflate(R.layout.bottom_navigation, relativeLayout)
            mode.menuInflater.inflate(R.menu.toc_action_menu, menu)

            navigationView = relativeLayout.findViewById(R.id.navigation) as BottomNavigationView
            navigationView.inflateMenu(R.menu.toc_menu)
            navigationView.itemIconTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked),
                    intArrayOf()),
                intArrayOf(requireContext().resources.getColor(R.color.colorPrimary, null),
                    Color.WHITE
                )
            )
            navigationView.setOnItemSelectedListener { item: MenuItem ->
                actionType = when (item.itemId) {
                    R.id.mark_read -> ActionType.MARK_READ
                    R.id.mark_unread -> ActionType.MARK_UNREAD
                    R.id.download -> ActionType.DOWNLOAD
                    R.id.delete_local -> ActionType.DELETE_SAVED_EPISODE
                    R.id.refresh -> ActionType.RELOAD
                    else -> {
                        showToast("Unknown Selected Item")
                        return@setOnItemSelectedListener false
                    }
                }
                displayActionModeActions()
                true
            }
            changeSelectionMode(SelectionMode.MULTI)
            inActionMode = true
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val adapter = getAdapter()
            when (item.itemId) {
                R.id.clear -> {
                    selectionTracker.clearSelection()
                    return true
                }
                R.id.select_between -> {
                    val positions = selectionTracker.selection.mapNotNull {
                        val position = getPositionFrom(it, listView)

                        if (position == RecyclerView.NO_POSITION) {
                            return@mapNotNull null
                        }
                        return@mapNotNull position
                    }

                    var highest = -1
                    var lowest = Int.MAX_VALUE

                    if (positions.isEmpty()) {
                        return true
                    }

                    for (position in positions) {
                        highest = max(position, highest)
                        lowest = min(position, lowest)
                    }

                    if (highest < 0) {
                        System.err.println("A selected positions which are not positive")
                        return true
                    }
                    // from (including) lowest to (including) highest
                    for (position in lowest..highest) {
                        val key = getKeyFrom(position, adapter)

                        if (key != null) {
                            selectionTracker.select(key)
                        }
                    }

                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mainActivity.requireSupportActionBar().show()
            relativeLayout.removeView(navigationView)
            actionType = null
            changeSelectionMode(SelectionMode.IDLE)
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
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        registerForContextMenu(listView)
        lifecycleScope.launch {
            val title = viewModel.getMediumTitle(mediumId)
            setTitle(title)
        }
        this.setTitle("Table of Contents")
        viewModel.setMediumId(mediumId)
        return view
    }

    override fun createAdapter(): BaseAdapter<TocEpisode, *> = TocAdapter()

    @ExperimentalCoroutinesApi
    override fun createPaged(model: TocEpisodeViewModel): Flow<PagingData<TocEpisode>> {
        return viewModel.toc
    }


    private class TocItemCallback : DiffUtil.ItemCallback<TocEpisode>() {
        override fun areItemsTheSame(oldItem: TocEpisode, newItem: TocEpisode): Boolean {
            return oldItem.episodeId == newItem.episodeId
        }

        override fun areContentsTheSame(oldItem: TocEpisode, newItem: TocEpisode): Boolean {
            return oldItem == newItem
        }
    }

    private class TocAdapter : BaseAdapter<TocEpisode, ViewHolder>(TocItemCallback()) {

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                val index: String = if (item.partialIndex > 0) {
                    "#${item.totalIndex}.${item.partialIndex}"
                } else {
                    "#${item.totalIndex}"
                }
                val earliestRelease = item.releases.minByOrNull { it.releaseDate!! }
                val topRight: String =
                    earliestRelease?.releaseDate?.toString("dd.MM.yyyy HH:mm:ss") ?: "Not Available"
                val title = item
                    .releases
                    .mapNotNull(Release::title)
                    .maxOfOrNull { it } ?: "Not available"

                val hasOnline = item.releases.any { it.url?.isNotBlank() ?: false }
                val isLocked = item.releases.all(Release::locked)

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
                holder.itemView.isActivated = selectionTracker.isSelected(item.getSelectionKey())
            } else {
                holder.itemView.isActivated = false
            }
        }

        override val layoutId = R.layout.episode_item

        override fun createViewHolder(root: View, viewType: Int) = ViewHolder(root)
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

    /**
     * TODO: What does this do?
     *  What purpose does this have?
     */
    override fun getPosition(text: String): Int {
        var position = super.getPosition(text)

        if (position < 0) {
            return position
        }
        val sort = viewModel.getSort()
        if (sort === Sortings.INDEX_DESC) {
            position = getAdapter().itemCount - position
        } else if (sort !== Sortings.INDEX_ASC) {
            showToast("Unknown Sort")
            position = -1
        }
        return position
    }

    override val viewModelClass: Class<TocEpisodeViewModel>
        get() = TocEpisodeViewModel::class.java

    override fun createFilterable(): Filterable {
        return object : Filterable {
            override val searchFilterProperties: Array<Property<*>>
                get() = arrayOf(
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.read

                        override fun get(): Int {
                            return viewModel.readFilter.toInt()
                        }

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun set(newFilter: Int) {
                            viewModel.readFilter = newFilter.toByte()
                        }
                    },
                    object : PositionProperty {
                        override val viewId: Int
                            get() = R.id.saved

                        override fun positionalMapping(): IntArray {
                            return intArrayOf(1, 0, -1)
                        }

                        override fun get(): Int {
                            return viewModel.savedFilter.toInt()
                        }

                        override fun set(newFilter: Int) {
                            viewModel.savedFilter = newFilter.toByte()
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

    override fun onItemClick(position: Int, item: TocEpisode?) {
        // in actionMode the selection tracker should handle these
        if (inActionMode) {
            return
        }

        if (item == null) {
            return
        }
        if (item.isSaved) {
            lifecycleScope.launch {
                val mediumType = instance.getMediumType(mediumId)
                openLocal(item.episodeId, mediumId, mediumType)
            }
        } else {
            val urls = item.releases.mapNotNull(Release::url)
            openInBrowser(urls)
        }
    }

    override fun onItemLongClick(position: Int, item: TocEpisode?): Boolean {
        if (!inActionMode) {
            this.mainActivity.startActionMode(callback)
            item?.getSelectionKey()?.let(selectionTracker::select)
            return true
        }
        return false
    }

    private fun displayActionModeActions() {
        if (actionType == null) {
            System.err.println("not action type selected")
            showToast("An Error occurred, cannot open Action Popup")
            return
        }
        val items: MutableList<TocEpisode> = ArrayList()
        for (selectedKey in selectionTracker.selection) {
            val position = getPositionFrom(selectedKey, listView)
            val item = getAdapter().getItemAt(position)

            if (item != null) {
                items.add(item)
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
        val title: String = when (actionType) {
            ActionType.RELOAD -> "Refresh:"
            ActionType.DOWNLOAD -> "Download:"
            ActionType.MARK_READ -> "Mark read:"
            ActionType.MARK_UNREAD -> "Mark unread:"
            ActionType.DELETE_SAVED_EPISODE -> "Delete Saved:"
            else -> {
                showToast("Unknown ActionType")
                return
            }
        }
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
                else -> println("Unknown ActionType: $actionType")
            }
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->
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

    @Suppress("BlockingMethodInNonBlockingContext")
    fun handle(selected: List<TocEpisode>, type: ActionType, count: ActionCount): Boolean {
        lifecycleScope.launch {
            if (mediumId <= 0 || selected.isEmpty()) {
                return@launch
            }
            val episodeIds: MutableSet<Int> = HashSet()
            val indices: MutableList<Double> = ArrayList()

            for (tocEpisode in selected) {
                episodeIds.add(tocEpisode.episodeId)
                val combiIndex = tocEpisode.toCombiIndex()
                indices.add(combiIndex)
            }
            try {
                when (type) {
                    ActionType.MARK_READ -> viewModel.updateRead(
                        episodeIds,
                        indices,
                        count,
                        mediumId,
                        true
                    )
                    ActionType.MARK_UNREAD -> viewModel.updateRead(
                        episodeIds,
                        indices,
                        count,
                        mediumId,
                        false
                    )
                    ActionType.DELETE_SAVED_EPISODE -> viewModel.deleteLocalEpisode(
                        episodeIds,
                        indices,
                        count,
                        mediumId
                    )
                    ActionType.DOWNLOAD -> viewModel.download(
                        episodeIds,
                        indices,
                        count,
                        mediumId
                    )
                    ActionType.RELOAD -> viewModel.reload(
                        episodeIds,
                        indices,
                        count,
                        mediumId
                    )
                    else -> println("Unknown ActionType: $type")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Could not execute Action")
            }
        }
        return true
    }

    private class ViewHolder(mView: View) : SelectableViewHolder(mView) {
        val textContentView: TextView = mView.findViewById(R.id.content) as TextView
        val textTopLeft: TextView = mView.findViewById(R.id.item_top_left) as TextView
        val textTopRight: TextView = mView.findViewById(R.id.item_top_right) as TextView
        val episodeLockedIcon: ImageView = mView.findViewById(R.id.episode_locked) as ImageView
        val episodeReadIcon: ImageView = mView.findViewById(R.id.episode_read) as ImageView
        val openBrowserIcon: ImageView = mView.findViewById(R.id.open_in_browser) as ImageView
        val openLocalIcon: ImageView = mView.findViewById(R.id.open_local) as ImageView

        override fun toString(): String {
            return "${super.toString()} '${textContentView.text}'"
        }

    }

    enum class ActionType {
        MARK_READ,
        MARK_UNREAD,
        MARK_PREVIOUSLY_READ,
        MARK_PREVIOUSLY_UNREAD,
        MARK_ALL_READ,
        MARK_ALL_UNREAD,
        DELETE_SAVED_EPISODE,
        DELETE_PREVIOUSLY_SAVED_EPISODE,
        DELETE_ALL_SAVED_EPISODE,
        DOWNLOAD,
        RELOAD
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