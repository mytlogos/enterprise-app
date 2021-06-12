package com.mytlogos.enterprise.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.ExternalMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.tools.getDomain
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Comparator

/**
 * A fragment representing a List of DisplayReleases.
 */
class EpisodeFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BasePagingFragment<DisplayRelease, EpisodeViewModel>() {
    private lateinit var episodeViewModel: EpisodeViewModel

    @ExperimentalCoroutinesApi
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

    override fun createFilterable(): Filterable {
        val showToastFunction = this::showToast
        return object : Filterable {
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
                object : TextProperty {
                    override val viewId = R.id.host_filter

                    override fun get() = viewModel.host

                    override fun set(newFilter: String) {
                        viewModel.host = newFilter.lowercase(Locale.getDefault())
                    }
                },
                object : PositionProperty {
                    override val viewId = R.id.read

                    override fun get() = viewModel.read

                    override fun positionalMapping(): IntArray = intArrayOf(1, 0, -1)

                    override fun set(newFilter: Int) {
                        viewModel.read = newFilter
                    }
                },
                object : PositionProperty {
                    override val viewId = R.id.saved

                    override fun positionalMapping() = intArrayOf(1, 0, -1)

                    override fun get() = viewModel.saved

                    override fun set(newFilter: Int) {
                        viewModel.saved = newFilter
                    }
                },
                SimpleProperty(R.id.latest_only, viewModel::isLatestOnly, showToastFunction),
            )

            override fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {
                val recycler: RecyclerView = view.findViewById(R.id.listsFilter) as RecyclerView
                val lists = viewModel.internLists
                val layoutManager = LinearLayoutManager(requireContext())
                recycler.layoutManager = layoutManager

                val decoration = DividerItemDecoration(requireContext(), layoutManager.orientation)
                recycler.addItemDecoration(decoration)

                val flexibleAdapter = FlexibleAdapter<IFlexible<*>>(null)
                updateRecycler(flexibleAdapter, lists.value)
                lists.observe(this@EpisodeFragment,
                    { mediaLists: MutableList<MediaList> ->
                        updateRecycler(flexibleAdapter,
                            mediaLists)
                    })

                flexibleAdapter.mode = SelectableAdapter.Mode.MULTI
                flexibleAdapter.addListener(FlexibleAdapter.OnItemClickListener { _: View?, position: Int ->
                    flexibleAdapter.toggleSelection(position)
                    val listIds: MutableList<Int> = ArrayList()

                    for (selectedPosition in flexibleAdapter.selectedPositions) {
                        val item =
                            flexibleAdapter.getItem(selectedPosition, FlexibleListItem::class.java)
                        item?.let { listIds.add(it.mediaList.listId) }
                    }

                    viewModel.filterListIds = listIds
                    sortFlexibleList(flexibleAdapter)
                    true
                })

                recycler.adapter = flexibleAdapter
            }

            fun updateRecycler(
                flexibleAdapter: FlexibleAdapter<IFlexible<*>>,
                mediaLists: MutableList<MediaList>?,
            ) {
                println("List: $mediaLists")
                if (mediaLists == null) {
                    return
                }
                val flexibles: MutableList<IFlexible<*>> =
                    mediaLists.map(::FlexibleListItem).toMutableList()

                this.sortFlexibleList(flexibleAdapter, flexibles)
            }

            private fun sortFlexibleList(
                flexibleAdapter: FlexibleAdapter<IFlexible<*>>,
                flexibles: MutableList<IFlexible<*>> = flexibleAdapter.currentItems.toMutableList(),
            ) {
                val listIds = viewModel.filterListIds

                flexibles.sortWith(Comparator { o1: IFlexible<*>, o2: IFlexible<*> ->
                    val item1 = o1 as FlexibleListItem
                    val item2 = o2 as FlexibleListItem

                    val selected1 = listIds.contains(item1.mediaList.listId)
                    val selected2 = listIds.contains(item2.mediaList.listId)

                    return@Comparator if (selected1 == selected2) {
                        item1.mediaList.name.compareTo(item2.mediaList.name)
                    } else {
                        if (selected1) -1 else 1
                    }
                })

                flexibleAdapter.updateDataSet(flexibles)

                // update selection
                for (i in 0 until flexibles.size) {
                    val item = flexibles[i] as FlexibleListItem

                    if (listIds.contains(item.mediaList.listId)) {
                        flexibleAdapter.addSelection(i)
                    } else {
                        flexibleAdapter.removeSelection(i)
                    }
                }
            }

            override val filterLayout: Int
                get() = R.layout.filter_unread_episode_layout
        }
    }

    private data class FlexibleListItem(val mediaList: MediaList) :
        AbstractFlexibleItem<TextOnlyViewHolder>() {

        override fun getLayoutRes(): Int {
            return R.layout.text_only_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?,
        ): TextOnlyViewHolder {
            return TextOnlyViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: TextOnlyViewHolder,
            position: Int,
            payloads: List<Any>,
        ) {
            val listSource = if (mediaList is ExternalMediaList) "External" else "Internal"
            val title = "${mediaList.name} ($listSource)"
            holder.textView.text = title

            val drawable = DrawableUtils.getSelectableBackgroundCompat(
                Color.WHITE, // normal background
                Color.GRAY,  // pressed background
                Color.BLACK, // ripple color
            )
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable)
        }
    }

    private class TextOnlyViewHolder(
        view: View,
        adapter: FlexibleAdapter<*>?,
    ) : FlexibleViewHolder(view, adapter) {
        val textView: TextView = view as TextView

        init {
            val params = textView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, 0)
            textView.textSize = 15f
            textView.requestLayout()
        }
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