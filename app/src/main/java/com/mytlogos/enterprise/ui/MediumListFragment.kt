package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.TimeAgo
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.requireSupportActionBar
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import com.mytlogos.enterprise.viewmodel.MediumViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*
import kotlin.math.max

class MediumListFragment : BasePagingFragment<MediumItem, MediumViewModel>() {
    private var inMoveMediumMode = false

    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Add Medium To List"
            mode.menuInflater.inflate(R.menu.add_medium_to_list_menu, menu)
            mainActivity.requireSupportActionBar().hide()
            changeSelectionMode(SelectionMode.MULTI)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId != R.id.add_item_to_list) {
                return false
            }

            val context = requireContext()
            val builder = AlertDialog.Builder(context)

            val listsViewModel = ViewModelProvider(this@MediumListFragment)
                .get(ListsViewModel::class.java)

            val adapter: ArrayAdapter<MediaList> = TextOnlyListAdapter(
                this@MediumListFragment,
                listsViewModel.internLists,
                MediaList::name
            )

            builder.setAdapter(adapter) { _: DialogInterface?, which: Int ->
                val list = adapter.getItem(which) ?: return@setAdapter

                val snapshot = getAdapter().snapshot().items
                val selectedMediaIds: MutableList<Int> = selectionTracker.selection
                    .mapNotNull { selectedKey ->
                        val mediumItem = snapshot.find { it.getSelectionKey() == selectedKey }
                            ?: return@mapNotNull null
                        return@mapNotNull mediumItem.mediumId
                    }.toMutableList()

                lifecycleScope.launch {
                    val success = listsViewModel.addMediumToList(list.listId, selectedMediaIds)
                    val text = if (success) {
                        mode.finish()
                        // TODO: 29.07.2019 replace toast with undoable snackbar
                        "Added ${selectedMediaIds.size} Media to ${list.name}"
                    } else {
                        "Could not add Media to List '${list.name}'"
                    }
                    showToast(text)
                }
            }
            builder.show()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mainActivity.requireSupportActionBar().show()
            changeSelectionMode(SelectionMode.IDLE)
            println("destroyed action mode")
            inMoveMediumMode = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Media")
        return view
    }

    private fun onItemLongClick(position: Int) {
        if (!inMoveMediumMode) {
            inMoveMediumMode = true
            println("starting move mode")
            val mode = this.mainActivity.startActionMode(callback)

            getAdapter().getItemAt(position)?.let {
                selectionTracker.select(it.getSelectionKey())
            }
            println("mode: $mode")
        }
    }

    private fun onItemClick(position: Int): Boolean {
        if (inMoveMediumMode) {
            return position != RecyclerView.NO_POSITION
        } else {
            val item = getAdapter().getItemAt(position) ?: return false
            val fragment: TocFragment = TocFragment.newInstance(item.mediumId)
            mainActivity.switchWindow(fragment, true)
        }
        return false
    }

    override val viewModelClass: Class<MediumViewModel>
        get() = MediumViewModel::class.java

    override fun createAdapter(): BaseAdapter<MediumItem, *> {
        val mediumAdapter = MediumAdapter()
        mediumAdapter.holderInit = BaseAdapter.ViewInit { holder ->
            holder.itemView.isLongClickable = true

            // add long click listener on view holder with a bound item
            holder.itemView.setOnLongClickListener {
                val position = holder.bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(position)
                }
                true
            }
            // add click listener on view holder with a bound item
            holder.itemView.setOnClickListener {
                val position = holder.bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
        return mediumAdapter
    }

    @ExperimentalCoroutinesApi
    override fun createPaged(model: MediumViewModel) = viewModel.allMedia

    private class MediumItemCallback : DiffUtil.ItemCallback<MediumItem>() {
        override fun areItemsTheSame(oldItem: MediumItem, newItem: MediumItem): Boolean {
            return oldItem.mediumId == newItem.mediumId
        }

        override fun areContentsTheSame(oldItem: MediumItem, newItem: MediumItem): Boolean {
            return oldItem == newItem
        }
    }

    private class MediumAdapter : BaseAdapter<MediumItem, NewMetaViewHolder>(MediumItemCallback()) {

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                val currentReadEpisode = max(item.currentReadEpisode, 0)
                val lastEpisode = max(item.lastEpisode, 0)
                val lastUpdated = item.lastUpdated
                val relativeTime: String = TimeAgo.toRelative(lastUpdated) ?: "No Updates"

                holder.topLeftText.text = "$currentReadEpisode/$lastEpisode"
                holder.topRightText.text = relativeTime
                holder.mainText.text = item.title

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

        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)
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
            override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {
                setNumberTextField(
                    view,
                    R.id.text_min_episode,
                    viewModel.minEpisodeFilter,
                )
                setNumberTextField(
                    view,
                    R.id.text_min_episode_read,
                    viewModel.minReadEpisodeFilter,
                )
            }

            override val filterLayout = R.layout.filter_medium_layout

            override val searchFilterProperties: Array<Property<*>>
                get() {
                    return arrayOf(
                        SimpleTextProperty(
                            R.id.title_filter,
                            viewModel::titleFilter,
                            this@MediumListFragment::showToast,
                            R.id.clear_title,
                        ),
                        SimpleTextProperty(
                            R.id.author_filter,
                            viewModel::authorFilter,
                            this@MediumListFragment::showToast,
                            R.id.clear_author,
                        ),
                    )
                }
        }
    }

    internal class FlexibleMediumItem(val item: MediumItem) :
        AbstractFlexibleItem<MetaViewHolder>() {

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = true
        }

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
            adapter: FlexibleAdapter<IFlexible<*>>,
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            val currentReadEpisode = if (item.currentReadEpisode < 0) 0 else item.currentReadEpisode
            val lastEpisode = if (item.lastEpisode < 0) 0 else item.lastEpisode
            holder.topLeftText.text = String.format("%d/%d", currentReadEpisode, lastEpisode)
            val lastUpdated = item.lastUpdated
            val relativeTime: String = if (lastUpdated != null) {
                TimeAgo.toRelative(lastUpdated, DateTime.now())!!
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
    }
}