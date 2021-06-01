package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.TimeAgo
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import com.mytlogos.enterprise.viewmodel.MediumViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import eu.davidea.viewholders.FlexibleViewHolder
import org.joda.time.DateTime
import java.util.*

class MediumListFragment : BaseListFragment<MediumItem, MediumViewModel>() {
    private var inMoveMediumMode = false
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Add Medium To List"
            mode.menuInflater.inflate(R.menu.add_medium_to_list_menu, menu)
            Objects.requireNonNull(mainActivity.supportActionBar)!!.hide()
            flexibleAdapter!!.mode = SelectableAdapter.Mode.MULTI
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId == R.id.add_item_to_list) {
                val context = requireContext()
                val builder = AlertDialog.Builder(context)
                val listsViewModel = ViewModelProvider(this@MediumListFragment)
                    .get(ListsViewModel::class.java)
                val adapter: ArrayAdapter<MediaList> = TextOnlyListAdapter(
                    this@MediumListFragment,
                    listsViewModel.internLists,
                    MediaList::name
                )
                builder.setAdapter(adapter) { dialog: DialogInterface?, which: Int ->
                    val list = adapter.getItem(which) ?: return@setAdapter
                    val selectedPositions = flexibleAdapter!!.selectedPositions
                    val selectedMediaIds: MutableList<Int> = ArrayList()
                    for (selectedPosition in selectedPositions) {
                        val flexible =
                            flexibleAdapter!!.getItem(selectedPosition) as? FlexibleMediumItem
                                ?: continue
                        val mediumItem = flexible.item
                        selectedMediaIds.add(mediumItem.mediumId)
                    }
                    val future = listsViewModel.addMediumToList(list.listId, selectedMediaIds)
                    future.whenComplete { aBoolean: Boolean?, throwable: Throwable? ->
                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post {
                            val text: String
                            if (aBoolean == null || !aBoolean || throwable != null) {
                                text = "Could not add Media to List '" + list.name + "'"
                            } else {
                                text = "Added " + selectedMediaIds.size + " Media to " + list.name
                                // TODO: 29.07.2019 replace toast with undoable snackbar
                                mode.finish()
                            }
                            val activity = activity
                            activity?.runOnUiThread { showToast(text) }
                        }
                    }
                }
                builder.show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Objects.requireNonNull(mainActivity.supportActionBar)!!.show()
            flexibleAdapter!!.mode = SelectableAdapter.Mode.IDLE
            flexibleAdapter!!.clearSelection()
            println("destroyed action mode")
            inMoveMediumMode = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Media")
        return view
    }

    override fun onItemLongClick(position: Int) {
        if (!inMoveMediumMode) {
            inMoveMediumMode = true
            println("starting move mode")
            val mode = this.mainActivity.startActionMode(callback)
            flexibleAdapter!!.addSelection(position)
            println("mode: $mode")
        }
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        if (inMoveMediumMode) {
            return if (position != RecyclerView.NO_POSITION) {
                flexibleAdapter!!.toggleSelection(position)
                true
            } else {
                false
            }
        } else {
            val item = flexibleAdapter!!.getItem(position) as? FlexibleMediumItem ?: return false
            val mediumItem = item.item
            val fragment: TocFragment = TocFragment.newInstance(mediumItem.mediumId)
            mainActivity.switchWindow(fragment, true)
        }
        return false
    }

    override val viewModelClass: Class<MediumViewModel>
        get() = MediumViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<MediumItem>> {
        return viewModel!!.allMedia!!
    }

    override fun createFlexible(value: MediumItem): IFlexible<*> {
        return FlexibleMediumItem(value)
    }

    override val sortMap: LinkedHashMap<String, Sortings>
        get() {
            val map = LinkedHashMap<String, Sortings>()
            map["Title A-Z"] = Sortings.TITLE_AZ
            map["Title Z-A"] = Sortings.TITLE_ZA
            map["Medium Asc"] = Sortings.MEDIUM
            map["Medium Desc"] = Sortings.MEDIUM_REVERSE
            map["Latest Update Asc"] = Sortings.LAST_UPDATE_ASC
            map["Latest Update Desc"] = Sortings.LAST_UPDATE_DESC
            map["Episodes Asc"] = Sortings.NUMBER_EPISODE_ASC
            map["Episodes Desc"] = Sortings.NUMBER_EPISODE_DESC
            map["Episodes Read Asc"] = Sortings.NUMBER_EPISODE_READ_ASC
            map["Episodes Read Desc"] = Sortings.NUMBER_EPISODE_READ_DESC
            map["Episodes UnRead Asc"] = Sortings.NUMBER_EPISODE_UNREAD_ASC
            map["Episodes UnRead Desc"] = Sortings.NUMBER_EPISODE_UNREAD_DESC
            return map
        }

    override fun createFilterable(): Filterable {
        return object : Filterable {
            @SuppressLint("SetTextI18n")
            override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {
                val minEpisodeFilter = viewModel!!.minEpisodeFilter
                setNumberTextField(view, R.id.text_min_episode, minEpisodeFilter, 0)
                val minReadEpisodeFilter = viewModel!!.minReadEpisodeFilter
                setNumberTextField(view, R.id.text_min_episode_read, minReadEpisodeFilter, 0)
            }

            override val filterLayout: Int
                get() = R.layout.filter_medium_layout
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
                            get() = R.id.author_filter
                        override val clearViewId: Int
                            get() = R.id.clear_author

                        override fun get(): String {
                            return viewModel!!.authorFilter
                        }

                        override fun set(newFilter: String) {
                            viewModel!!.authorFilter = newFilter
                        }
                    }
                )
        }
    }

    internal class FlexibleMediumItem(val item: MediumItem) :
        AbstractFlexibleItem<MetaViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FlexibleMediumItem) return false
            return item.mediumId == other.item.mediumId
        }

        override fun hashCode(): Int {
            return item.mediumId
        }

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
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            val currentReadEpisode = if (item.currentReadEpisode < 0) 0 else item.currentReadEpisode
            val lastEpisode = if (item.lastEpisode < 0) 0 else item.lastEpisode
            holder.topLeftText.text = String.format("%d/%d", currentReadEpisode, lastEpisode)
            val relativeTime: CharSequence
            val lastUpdated = item.lastUpdated
            relativeTime = if (lastUpdated != null) {
                TimeAgo.toRelative(lastUpdated, DateTime.now())
            } else {
                "No Updates"
            }
            holder.topRightText.text = relativeTime
            holder.mainText.text = item.title
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

    private class SectionableListItem(
        private val item: MediumItem,
        private val fragment: BaseFragment
    ) : AbstractSectionableItem<ViewHolder, HeaderItem?>(
        HeaderItem(
            item.author)) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SectionableListItem) return false
            return item.mediumId == other.item.mediumId
        }

        override fun hashCode(): Int {
            return item.mediumId
        }

        override fun getLayoutRes(): Int {
            return R.layout.list_item
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
            holder.mItem = item
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.text = String.format("%d/%d", item.currentReadEpisode, item.lastEpisode)
            val relativeTime: CharSequence
            val lastUpdated = item.lastUpdated
            relativeTime = if (lastUpdated != null) {
                TimeAgo.toRelative(lastUpdated, DateTime.now())
            } else {
                "No Updates"
            }
            holder.denominatorView.text = relativeTime
            holder.contentView.text = item.title
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = false
        }
    }

    private class HeaderItem(private val title: String) : AbstractHeaderItem<HeaderViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as HeaderItem
            return title == that.title
        }

        override fun hashCode(): Int {
            return title.hashCode()
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

    internal class ViewHolder(mView: View, adapter: FlexibleAdapter<*>?) : FlexibleViewHolder(
        mView, adapter) {
        val contentView: TextView = mView.findViewById(R.id.content)
        val metaView: TextView = mView.findViewById(R.id.item_top_left)
        val denominatorView: TextView = mView.findViewById(R.id.item_top_right)
        var mItem: MediumItem? = null
        override fun shouldAddSelectionInActionMode(): Boolean {
            return true
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

    }
}