package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Utils.getDomain
import com.mytlogos.enterprise.viewmodel.MediumInWaitViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import java.io.Serializable
import java.util.*

class MediaInWaitFragment : BaseFragment() {
    private val NO_LIST = MediaList("", 0, "Don't add to List", 0, 0)
    private var selectedMedium: SimpleMedium? = null
    private var viewModel: MediumInWaitViewModel? = null
    private var mediumInWait: MediumInWait? = null
    private var addMedium: RadioButton? = null
    private var addToc: RadioButton? = null
    private var listSelect: Spinner? = null
    private var listSelectContainer: View? = null
    private var running = false
    private var searchMediumView: SearchView? = null
    private var searchMediumInWaitView: SearchView? = null
    private val selectedInWaits: MutableSet<MediumInWait> = HashSet()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.medium_in_wait, container, false)
        val bundle = requireArguments()
        mediumInWait = bundle.getSerializable(MEDIUM_IN_WAIT) as MediumInWait
        requireNotNull(mediumInWait) { "no arguments" }
        val titleView = view.findViewById<TextView>(R.id.title)
        val domain = getDomain(mediumInWait!!.link)
        titleView.text = String.format("%s (%s)", mediumInWait!!.title, domain)
        viewModel = ViewModelProvider(this).get(MediumInWaitViewModel::class.java)
        val listAdapter = getFlexibleRecyclerAdapter(view, R.id.list)
        val mediumSuggestAdapter = getFlexibleRecyclerAdapter(view, R.id.medium_suggestions)
        val mediumInWaitSuggestAdapter =
            getFlexibleRecyclerAdapter(view, R.id.medium_in_wait_suggestions)
        mediumSuggestAdapter.addListener(FlexibleAdapter.OnItemClickListener { _: View?, position: Int ->
            val flexible =
                mediumSuggestAdapter.getItem(position)!! as? FlexibleMedium
                    ?: return@OnItemClickListener false
            selectedMedium = flexible.medium
            searchMediumView!!.setQuery(flexible.medium.title, false)
            false
        })
        mediumInWaitSuggestAdapter.addListener(FlexibleAdapter.OnItemClickListener { _: View?, position: Int ->
            val flexible =
                mediumInWaitSuggestAdapter.getItem(position)!! as? FlexibleMediumInWaitSuggestion
                    ?: return@OnItemClickListener false
            var filter = mediumInWaitSuggestAdapter.getFilter(
                MediumInWaitSimpleFilter::class.java)
            if (filter == null) {
                filter = MediumInWaitSimpleFilter()
            }
            filter.filterOut.add(flexible.mediumInWait)
            selectedInWaits.add(flexible.mediumInWait)
            listAdapter.addItem(FlexibleMediumInWait(flexible.mediumInWait))
            mediumInWaitSuggestAdapter.setFilter(filter)
            mediumInWaitSuggestAdapter.filterItems()
            false
        })
        searchMediumView = view.findViewById(R.id.search_medium_view)
        searchMediumView!!.setOnQueryTextListener(searchMediumListener())
        searchMediumInWaitView = view.findViewById(R.id.search_medium_in_wait_view)
        searchMediumInWaitView!!.setOnQueryTextListener(searchMediumInWaitListener())
        listSelect = view.findViewById(R.id.list_select)
        listSelectContainer = view.findViewById(R.id.list_select_container)
        listSelect!!.adapter = TextOnlyListAdapter(
            this,
            Transformations.map(
                viewModel!!.getInternalLists()) { input: MutableList<MediaList> ->
                input.add(0, NO_LIST)
                input
            },
            MediaList::name
        )
        addMedium = view.findViewById(R.id.add_medium)
        addToc = view.findViewById(R.id.add_toc)
        switchAddMode(searchMediumView, addMedium!!.isChecked)
        addMedium!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            switchAddMode(searchMediumView,
                isChecked)
        }
        view.findViewById<View>(R.id.cancel_button)
            .setOnClickListener { requireActivity().onBackPressed() }
        view.findViewById<View>(R.id.add_btn)
            .setOnClickListener { process(listAdapter) }
        val similarMediaInWait = viewModel!!.getSimilarMediaInWait(mediumInWait)
        similarMediaInWait.observe(viewLifecycleOwner, { mediumInWaits: MutableList<MediumInWait> ->
            val list: MutableList<IFlexible<*>> = ArrayList()
            mediumInWaits.addAll(selectedInWaits)
            for (inWait in mediumInWaits) {
                val flexible = FlexibleMediumInWait(inWait)
                if (flexible.mediumInWait != mediumInWait) {
                    list.add(flexible)
                }
            }
            listAdapter.updateDataSet(list)
        })
        val inWaitSuggestions = viewModel!!.getMediumInWaitSuggestions(
            mediumInWait!!.medium)
        inWaitSuggestions.observe(viewLifecycleOwner, { mediumInWaits: List<MediumInWait> ->
            mediumInWaitSuggestAdapter.updateDataSet(
                mediumInWaits
                    .map { mediumInWait: MediumInWait -> FlexibleMediumInWaitSuggestion(mediumInWait) }
            )
        })
        val mediumSuggestions = viewModel!!.getMediumSuggestions(
            mediumInWait!!.medium)
        mediumSuggestions.observe(viewLifecycleOwner, { medium: List<SimpleMedium> ->
            mediumSuggestAdapter.updateDataSet(
                medium.map(::FlexibleMedium)
            )
        })
        return view
    }

    private class MediumInWaitSimpleFilter : Serializable {
        val filterOut: MutableSet<MediumInWait> = HashSet()
    }

    private fun searchMediumListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel!!.setMediumTitleFilter(newText)
                return true
            }
        }
    }

    private fun searchMediumInWaitListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel!!.setMediumInWaitTitleFilter(newText)
                return true
            }
        }
    }

    private fun switchAddMode(searchMediumView: SearchView?, addMedium: Boolean) {
        if (addMedium) {
            listSelectContainer!!.visibility = View.VISIBLE
            searchMediumView!!.visibility = View.GONE
        } else {
            listSelectContainer!!.visibility = View.GONE
            searchMediumView!!.visibility = View.VISIBLE
        }
    }

    private fun process(listAdapter: FlexibleAdapter<IFlexible<*>>) {
        if (running) {
            return
        }
        if (mediumInWait == null) {
            return
        }
        running = true
        if (addMedium!!.isChecked) {
            val items = listAdapter.currentItems
            val mediumInWaits: MutableList<MediumInWait> = ArrayList()
            for (item in items) {
                if (item is FlexibleMediumInWait) {
                    mediumInWaits.add(item.mediumInWait)
                }
            }
            var item: MediaList? = listSelect!!.selectedItem as MediaList
            if (item == NO_LIST) {
                item = null
            }
            val success = viewModel!!.createMedium(mediumInWait!!, mediumInWaits, item)
            success.whenComplete { aBoolean: Boolean?, throwable: Throwable? ->
                val msg: String = if (aBoolean == null || !aBoolean || throwable != null) {
                    "Could not create Medium"
                } else {
                    "Created a Medium and consumed " + mediumInWaits.size + " other unused Media"
                }
                running = false
                requireActivity().runOnUiThread {
                    showToast(msg)
                    requireActivity().onBackPressed()
                }
            }
        } else if (addToc!!.isChecked) {
            if (selectedMedium == null) {
                showToast("No Medium selected")
                return
            }
            val items = listAdapter.currentItems
            val mediumInWaits: MutableList<MediumInWait> = ArrayList()
            for (item in items) {
                if (item is FlexibleMediumInWait) {
                    mediumInWaits.add(item.mediumInWait)
                }
            }
            mediumInWaits.add(mediumInWait!!)
            val success = viewModel!!.consumeMediumInWait(selectedMedium!!, mediumInWaits)
            success.whenComplete { aBoolean: Boolean?, throwable: Throwable? ->
                val msg: String = if (aBoolean == null || !aBoolean || throwable != null) {
                    "Could not process Media"
                } else {
                    "Consumed " + mediumInWaits.size + " Media"
                }
                running = false
                requireActivity().runOnUiThread {
                    showToast(msg)
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun getFlexibleRecyclerAdapter(view: View, id: Int): FlexibleAdapter<IFlexible<*>> {
        val recyclerView: RecyclerView = view.findViewById(id)
        // Set the adapter
        val context = view.context
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val decoration = DividerItemDecoration(context, layoutManager.orientation)
        recyclerView.addItemDecoration(decoration)
        val adapter = FlexibleAdapter<IFlexible<*>>(null)
        recyclerView.adapter = adapter
        return adapter
    }

    private class FlexibleMediumInWait(val mediumInWait: MediumInWait) :
        AbstractFlexibleItem<CloseableTextViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as FlexibleMediumInWait
            return mediumInWait == that.mediumInWait
        }

        override fun hashCode(): Int {
            return mediumInWait.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.closeable_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>
        ): CloseableTextViewHolder {
            return CloseableTextViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: CloseableTextViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val domain = getDomain(mediumInWait.link)
            val title = String.format("%s (%s)", mediumInWait.title, domain)
            holder.textView.text = title
        }
    }

    private class FlexibleMedium(val medium: SimpleMedium) :
        AbstractFlexibleItem<CloseableTextViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as FlexibleMedium
            return medium == that.medium
        }

        override fun hashCode(): Int {
            return medium.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.text_only_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>
        ): CloseableTextViewHolder {
            return CloseableTextViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: CloseableTextViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            holder.textView.text = medium.title
        }
    }

    private class FlexibleMediumInWaitSuggestion(val mediumInWait: MediumInWait) :
        AbstractFlexibleItem<CloseableTextViewHolder>(), IFilterable<MediumInWaitSimpleFilter?> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as FlexibleMediumInWaitSuggestion
            return mediumInWait == that.mediumInWait
        }

        override fun hashCode(): Int {
            return mediumInWait.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.text_only_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>
        ): CloseableTextViewHolder {
            return CloseableTextViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: CloseableTextViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val domain = getDomain(mediumInWait.link)
            val title = String.format("%s (%s)", mediumInWait.title, domain)
            holder.textView.text = title
        }

        override fun filter(constraint: MediumInWaitSimpleFilter?): Boolean {
            return if (constraint == null) {
                true
            } else !constraint.filterOut.contains(mediumInWait)
        }
    }

    companion object {
        private const val MEDIUM_IN_WAIT = "MEDIUM_IN_WAIT"
        fun getInstance(inWait: MediumInWait?): MediaInWaitFragment {
            val fragment = MediaInWaitFragment()
            val bundle = Bundle()
            bundle.putSerializable(MEDIUM_IN_WAIT, inWait)
            fragment.arguments = bundle
            return fragment
        }
    }
}