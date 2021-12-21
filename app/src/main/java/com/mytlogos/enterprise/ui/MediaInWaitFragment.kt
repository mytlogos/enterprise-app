package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.getDomain
import com.mytlogos.enterprise.viewmodel.MediumInWaitViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
class MediaInWaitFragment : BaseFragment() {
    private var selectedMedium: SimpleMedium? = null
    private lateinit var mediumInWait: MediumInWait
    private lateinit var viewModel: MediumInWaitViewModel
    private lateinit var addMedium: RadioButton
    private lateinit var addToc: RadioButton
    private lateinit var listSelect: Spinner
    private lateinit var listSelectContainer: View
    private lateinit var searchMediumView: SearchView
    private lateinit var searchMediumInWaitView: SearchView
    private var running = false
    private val selectedInWaits: MutableSet<MediumInWait> = HashSet()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.medium_in_wait, container, false)

        val bundle = requireArguments()
        val mediumInWait = bundle.getSerializable(MEDIUM_IN_WAIT) as MediumInWait?
        requireNotNull(mediumInWait) { "no arguments" }
        this.mediumInWait = mediumInWait

        val titleView = view.findViewById<TextView>(R.id.title)
        val domain = getDomain(mediumInWait.link)
        titleView.text = "${mediumInWait.title} ($domain)"
        viewModel = ViewModelProvider(this).get(MediumInWaitViewModel::class.java)

        val listAdapter = createRecyclerAdapter(view, R.id.list) { MediumInWaitAdapter(closable = true) }
        val mediumSuggestAdapter = createRecyclerAdapter(view, R.id.medium_suggestions) { MediumAdapter() }
        val mediumInWaitSuggestAdapter = createRecyclerAdapter(view, R.id.medium_in_wait_suggestions) { MediumInWaitAdapter() }

        mediumSuggestAdapter.addItemClickListener { position: Int ->
            val flexible =
                mediumSuggestAdapter.getItemAt(position)!! as? SimpleMedium
                    ?: return@addItemClickListener
            selectedMedium = flexible
            searchMediumView.setQuery(flexible.title, false)
        }

        mediumInWaitSuggestAdapter.addItemClickListener { position: Int ->
            val similarMediumInWait =
                mediumInWaitSuggestAdapter.getItemAt(position)!! as? MediumInWait
                    ?: return@addItemClickListener

            selectedInWaits.add(similarMediumInWait)
            listAdapter.addItem(similarMediumInWait)
            mediumInWaitSuggestAdapter.ignoreItems(listAdapter.currentItems)
        }

        searchMediumView = view.findViewById(R.id.search_medium_view)
        searchMediumView.setOnQueryTextListener(searchMediumListener())
        searchMediumInWaitView = view.findViewById(R.id.search_medium_in_wait_view)
        searchMediumInWaitView.setOnQueryTextListener(searchMediumInWaitListener())
        listSelect = view.findViewById(R.id.list_select)
        listSelectContainer = view.findViewById(R.id.list_select_container)
        listSelect.adapter = TextOnlyListAdapter(
            this,
            Transformations.map(viewModel.getInternalLists()) { input: MutableList<MediaList> ->
                input.add(0, NO_LIST)
                input
            },
            MediaList::name
        )
        addMedium = view.findViewById(R.id.add_medium)
        addToc = view.findViewById(R.id.add_toc)
        switchAddMode(searchMediumView, addMedium.isChecked)
        addMedium.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            switchAddMode(searchMediumView,
                isChecked)
        }
        view.findViewById<View>(R.id.cancel_button).setOnClickListener { requireActivity().onBackPressed() }
        view.findViewById<View>(R.id.add_btn).setOnClickListener { process(listAdapter) }

        val similarMediaInWait = viewModel.getSimilarMediaInWait(mediumInWait)
        similarMediaInWait.observe(viewLifecycleOwner, { mediumInWaits: MutableList<MediumInWait> ->
            mediumInWaits.addAll(selectedInWaits)
            listAdapter.setItems(mediumInWaits)
        })
        val inWaitSuggestions = viewModel.getMediumInWaitSuggestions(mediumInWait.medium)
        inWaitSuggestions.observe(viewLifecycleOwner, { mediumInWaits: List<MediumInWait> ->
            mediumInWaitSuggestAdapter.setItems(ArrayList(mediumInWaits))
        })

        val mediumSuggestions = viewModel.getMediumSuggestions(mediumInWait.medium)
        mediumSuggestions.observe(viewLifecycleOwner, { medium: List<SimpleMedium> ->
            mediumSuggestAdapter.setItems(ArrayList(medium))
        })
        return view
    }

    private fun searchMediumListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.setMediumTitleFilter(newText)
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
                viewModel.setMediumInWaitTitleFilter(newText)
                return true
            }
        }
    }

    private fun switchAddMode(searchMediumView: SearchView, addMedium: Boolean) {
        if (addMedium) {
            listSelectContainer.visibility = View.VISIBLE
            searchMediumView.visibility = View.GONE
        } else {
            listSelectContainer.visibility = View.GONE
            searchMediumView.visibility = View.VISIBLE
        }
    }

    private fun process(listAdapter: MediumInWaitAdapter) {
        if (running) {
            return
        }
        running = true

        if (addMedium.isChecked) {
            val items = listAdapter.currentItems
            val mediumInWaits: List<MediumInWait> = items

            var item: MediaList? = listSelect.selectedItem as MediaList?

            if (item == null) {
                item = NO_LIST
            }
            lifecycleScope.launch {
                val success = runCatching {
                    viewModel.createMedium(mediumInWait, mediumInWaits, item)
                }

                val msg: String = if (success.isFailure) {
                    "Could not create Medium"
                } else {
                    "Created a Medium and consumed ${mediumInWaits.size} other unused Media"
                }

                running = false
                showToast(msg)
                requireActivity().onBackPressed()
            }
        } else if (addToc.isChecked) {
            val localSelectedMedium = selectedMedium

            if (localSelectedMedium == null) {
                showToast("No Medium selected")
                return
            }
            val items = listAdapter.currentItems
            val mediumInWaits: MutableList<MediumInWait> = items.toMutableList()
            mediumInWaits.add(mediumInWait)

            lifecycleScope.launch {
                val success = runCatching {
                    viewModel.consumeMediumInWait(localSelectedMedium, mediumInWaits)
                }

                val msg: String = if (success.isFailure) {
                    "Could not process Media"
                } else {
                    "Consumed ${mediumInWaits.size} Media"
                }

                running = false
                showToast(msg)
                requireActivity().onBackPressed()
            }
        }
    }

    private fun <T : RecyclerView.Adapter<*>> createRecyclerAdapter(view: View, @IdRes id: Int, create: () -> T): T {
        val recyclerView: RecyclerView = view.findViewById(id)
        // Set the adapter
        val context = view.context
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val decoration = DividerItemDecoration(context, layoutManager.orientation)
        recyclerView.addItemDecoration(decoration)

        val adapter = create()
        recyclerView.adapter = adapter
        return adapter
    }

    private class MediumInWaitAdapter(val closable: Boolean = false): RecyclerView.Adapter<CloseableTextViewHolder>(), ItemPositionable<MediumInWait> {
        private lateinit var itemClickListener: (position: Int) -> Unit
        private var items: MutableList<MediumInWait> = ArrayList()
        private var ignoreItems: List<MediumInWait> = ArrayList()

        val currentItems: List<MediumInWait>
            get() = items

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloseableTextViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(
                if (closable) R.layout.closeable_item else R.layout.text_only_item,
                parent,
                false
            )
            val holder = CloseableTextViewHolder(root, removeCallback = { position -> items.removeAt(position) }, closable)
            root.setOnClickListener { itemClickListener(holder.bindingAdapterPosition) }
            return holder
        }

        override fun onBindViewHolder(holder: CloseableTextViewHolder, position: Int) {
            getItemAt(position)?.let {
                val domain = getDomain(it.link)
                val title = "${it.title} ($domain)"
                holder.textView.text = title
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItems(items: MutableList<MediumInWait>) {
            this.items = items
            this.items.removeAll(this.ignoreItems.toSet())
            this.notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItemAt(position: Int): MediumInWait? {
            if (position >= 0 && position < items.size) {
                return items[position]
            }
            return null
        }
        fun addItemClickListener(itemClickListener: (position: Int) -> Unit) {
            this.itemClickListener = itemClickListener
        }

        fun addItem(mediumInWait: MediumInWait) {
            this.items.add(mediumInWait)
            this.notifyItemInserted(this.items.size)
        }

        fun ignoreItems(ignore: List<MediumInWait>) {
            this.items.removeAll(ignore.toSet())
            this.ignoreItems = ignore
        }
    }

    private class MediumAdapter: RecyclerView.Adapter<CloseableTextViewHolder>(), ItemPositionable<SimpleMedium> {
        private var items: MutableList<SimpleMedium> = ArrayList()
        private lateinit var itemClickListener: (position: Int) -> Unit

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloseableTextViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(
                R.layout.text_only_item,
                parent,
                false
            )
            val holder = CloseableTextViewHolder(root, removeCallback = { position -> items.removeAt(position) }, false)
            root.setOnClickListener { itemClickListener(holder.bindingAdapterPosition) }
            return holder
        }

        override fun onBindViewHolder(holder: CloseableTextViewHolder, position: Int) {
            getItemAt(position)?.let {
                holder.textView.text = it.title
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItems(items: MutableList<SimpleMedium>) {
            this.items = items
            this.notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return this.items.size
        }

        override fun getItemAt(position: Int): SimpleMedium? {
            if (position >= 0 && position < items.size) {
                return items[position]
            }
            return null
        }
        fun addItemClickListener(itemClickListener: (position: Int) -> Unit) {
            this.itemClickListener = itemClickListener
        }
    }

    companion object {
        private const val MEDIUM_IN_WAIT = "MEDIUM_IN_WAIT"
        private val NO_LIST = MediaList("", -1, "Don't add to List", 0, 0)

        fun getInstance(inWait: MediumInWait): MediaInWaitFragment {
            val bundle = Bundle()
            bundle.putSerializable(MEDIUM_IN_WAIT, inWait)

            val fragment = MediaInWaitFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}