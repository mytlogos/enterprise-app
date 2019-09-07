package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.FilterableViewModel;
import com.mytlogos.enterprise.viewmodel.MediumFilterableViewModel;
import com.mytlogos.enterprise.viewmodel.SortableViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

abstract class BaseListFragment<Value, ViewModel extends AndroidViewModel> extends BaseFragment
        implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.EndlessScrollListener,
        FlexibleAdapter.OnActionStateListener, FlexibleAdapter.OnDeleteCompleteListener,
        FlexibleAdapter.OnFilterListener, FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.OnItemMoveListener, FlexibleAdapter.OnItemSwipeListener,
        FlexibleAdapter.OnStickyHeaderChangeListener, FlexibleAdapter.OnUpdateListener {

    private FlexibleAdapter<IFlexible> flexibleAdapter;
    private ViewModel viewModel;
    private LiveData<PagedList<Value>> livePagedList;
    private View listContainer;
    private ViewGroup fragmentRoot;
    private Filterable filterable;
    private final Observer<PagedList<Value>> pagedListObserver = items -> {
        if (checkEmptyList(items, this.fragmentRoot, this.listContainer)) {
            System.out.println("empty dataset");
            flexibleAdapter.updateDataSet(null);
            return;
        }
        List<IFlexible> flexibles = convertToFlexibles(items);
        flexibleAdapter.updateDataSet(flexibles);
    };
    private RecyclerView recyclerView;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO: 28.07.2019 when items where loaded and
        //  then a swipe refresh follow somewhere, everything will disappear
        fragmentRoot = (ViewGroup) inflater.inflate(getLayoutId(), container, false);

        FloatingActionButton button = fragmentRoot.findViewById(R.id.fab);
        button.setTag(R.drawable.ic_arrow_down_bright);
        button.setOnLongClickListener(v -> {
            int newDrawableResource;
            if (((Integer) R.drawable.ic_arrow_down_bright).equals(button.getTag())) {
                newDrawableResource = R.drawable.ic_arrow_up_bright;
            } else {
                newDrawableResource = R.drawable.ic_arrow_down_bright;
            }
            button.setImageResource(newDrawableResource);
            button.setTag(newDrawableResource);
            return true;
        });

        button.setOnClickListener(v -> {
            if (flexibleAdapter.getItemCount() == 0) {
                return;
            }
            if (((Integer) R.drawable.ic_arrow_down_bright).equals(button.getTag())) {
                flexibleAdapter.smoothScrollToPosition(flexibleAdapter.getItemCount() - 1);
            } else {
                flexibleAdapter.smoothScrollToPosition(0);
            }
        });

        recyclerView = fragmentRoot.findViewById(R.id.list);

        // Set the adapter
        Context context = fragmentRoot.getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        flexibleAdapter = new FlexibleAdapter<>(null)
                .setStickyHeaders(true)
                .setDisplayHeadersAtStartUp(true);

        flexibleAdapter.setEndlessScrollListener(this, new ProgressItem());
        flexibleAdapter.addListener(this);

        this.onFlexibleCreated(flexibleAdapter);
        recyclerView.setAdapter(flexibleAdapter);

        this.viewModel = this.createViewModel();
        this.listContainer = fragmentRoot.findViewById(getListContainerId());

        this.setLivePagedList(this.createPagedListLiveData());

        this.setHasOptionsMenu(true);
        this.filterable = createFilterable();
        return fragmentRoot;
    }

    private void openFilter() {
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(this.filterable.getFilterLayout(), null);

        if (this.filterable.getSearchFilterProperties() != null) {

            for (FilterProperty filterProperty : this.filterable.getSearchFilterProperties()) {
                SearchView searchView = view.findViewById(filterProperty.getSearchViewId());

                String filter = filterProperty.get();
                searchView.setQuery(filter, false);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProperty.set(newText);
                        return false;
                    }
                });

                int clearSearchButtonId = filterProperty.getClearSearchButtonId();

                if (clearSearchButtonId != View.NO_ID) {
                    ImageButton clearTitleButton = view.findViewById(clearSearchButtonId);
                    clearTitleButton.setOnClickListener(v -> searchView.setQuery("", true));
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog
                .Builder(this.getMainActivity())
                .setView(view);

        this.filterable.onCreateFilter(view, builder);
        builder
                .setNeutralButton("Reset Filter", (dialog, which) -> {
                    if (getViewModel() instanceof FilterableViewModel) {
                        ((FilterableViewModel) getViewModel()).resetFilter();
                    } else {
                        this.filterable.onResetFilter();
                    }
                })
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (this.filterable != null) {
            inflater.inflate(R.menu.filter_menu, menu);
        }
        Map<String, Sortings> sortMap = this.getSortMap();

        if (sortMap != null && !sortMap.isEmpty()) {
            inflater.inflate(R.menu.sort_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_menu:
                openFilter();
                return true;
            case R.id.sort_menu:
                onSortMenuClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("WeakerAccess")
    final void setLivePagedList(LiveData<PagedList<Value>> livePagedList) {
        if (this.livePagedList != null) {
            this.livePagedList.removeObserver(pagedListObserver);
        }
        this.livePagedList = livePagedList;
        this.livePagedList.observe(this, pagedListObserver);
    }

    @Override
    public void noMoreLoad(int newItemsSize) {
        System.out.println("nothing to load anymore");
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        PagedList<Value> pagedList = livePagedList.getValue();

        if (pagedList == null) {
            return;
        }
        if (lastPosition >= pagedList.size()) {
            flexibleAdapter.onLoadMoreComplete(null);
            return;
        }
        List<Value> snapshot = pagedList.snapshot();
        pagedList.loadAround(lastPosition);
        pagedList.addWeakCallback(snapshot, new PagedList.Callback() {
            @Override
            public void onChanged(int position, int count) {
                List<Value> values = pagedList.subList(position, position + count);
                List<IFlexible> newItems = convertToFlexibles(values);

                newItems.removeAll(getFlexibleAdapter().getCurrentItems());
                flexibleAdapter.onLoadMoreComplete(newItems);
            }

            @Override
            public void onInserted(int position, int count) {
                System.out.println(String.format("Position: %s and Count: %s - Inserted", position, count));
            }

            @Override
            public void onRemoved(int position, int count) {
                System.out.println(String.format("Position: %s and Count: %s - Removed", position, count));
            }
        });
        System.out.println("loading more");
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {

    }

    @Override
    public void onDeleteConfirmed(int event) {

    }

    @Override
    public void onUpdateFilterView(int size) {

    }

    @Override
    public boolean onItemClick(View view, int position) {
        return false;
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public boolean shouldMoveItem(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemSwipe(int position, int direction) {

    }

    @Override
    public void onStickyHeaderChange(int newPosition, int oldPosition) {

    }

    @Override
    public void onUpdateEmptyView(int size) {

    }

    void onFlexibleCreated(FlexibleAdapter<IFlexible> adapter) {

    }

    @SuppressLint("SetTextI18n")
    public void setNumberTextField(View view, @IdRes int id, int value, int minValue) {
        EditText minEpisodeRead = view.findViewById(id);

        if (value < minValue) {
            minEpisodeRead.setText(null);
        } else {
            minEpisodeRead.setText(Integer.toString(value));
        }
    }

    interface Filterable {
        void onCreateFilter(View view, AlertDialog.Builder builder);

        int getFilterLayout();

        default void onResetFilter() {

        }

        default FilterProperty[] getSearchFilterProperties() {
            return new FilterProperty[0];
        }
    }

    interface FilterProperty {
        @IdRes
        int getSearchViewId();

        @IdRes
        int getClearSearchButtonId();

        String get();

        void set(String newFilter);
    }

    @Nullable
    Filterable createFilterable() {
        return null;
    }

    @LayoutRes
    public int getLayoutId() {
        return R.layout.normal_list;
    }

    final ViewModel getViewModel() {
        return viewModel;
    }

    final LiveData<PagedList<Value>> getLivePagedList() {
        return livePagedList;
    }

    View getListContainer() {
        return listContainer;
    }

    final RecyclerView getListView() {
        return recyclerView;
    }

    @IdRes
    int getListContainerId() {
        return R.id.list;
    }

    final FlexibleAdapter<IFlexible> getFlexibleAdapter() {
        return flexibleAdapter;
    }

    final List<Value> getItems() {
        List<Value> values = new ArrayList<>();
        PagedList<Value> pagedList = this.livePagedList.getValue();

        if (pagedList == null) {
            return values;
        }
        for (Value value : pagedList) {
            if (value == null) {
                break;
            }
            values.add(value);
        }
        return values;
    }

    public <E> void setStringSpinner(View view, @IdRes int resId, LinkedHashMap<String, E> valueMap, Consumer<E> consumer) {
        String[] items = valueMap.keySet().toArray(new String[0]);

        Spinner readSpinner = view.findViewById(resId);
        TextOnlyListAdapter<String> readAdapter = new TextOnlyListAdapter<>(requireContext(), null);
        readAdapter.addAll(items);

        readSpinner.setAdapter(readAdapter);
        readSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = items[position];
                consumer.accept(valueMap.get(item));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    LinkedHashMap<String, Sortings> getSortMap() {
        return new LinkedHashMap<>();
    }

    void onSortingChanged(Sortings sortings) {
        if (this.getViewModel() instanceof SortableViewModel) {
            ((SortableViewModel) this.getViewModel()).setSort(sortings);
        }
    }

    void onSortMenuClicked() {
        LinkedHashMap<String, Sortings> map = getSortMap();
        String[] strings = map.keySet().toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
        builder.setItems(strings, (dialog, which) -> {
            if (which < strings.length && which >= 0) {
                String title = strings[which];
                Sortings sortings = map.get(title);
                onSortingChanged(sortings);
            }
        });

        builder.setTitle("Sort By");
        builder.create().show();
    }

    void setMediumCheckbox(View view, @IdRes int boxId, int type) {
        ViewModel model = getViewModel();
        if (!(model instanceof MediumFilterableViewModel)) {
            throw new IllegalStateException("ViewModel not instance of MediumFilterableViewModel");
        }
        MediumFilterableViewModel filterableViewModel = (MediumFilterableViewModel) model;
        int medium = filterableViewModel.getMediumFilter();

        CheckBox box = view.findViewById(boxId);
        box.setChecked(MediumType.is(medium, type));
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int filter = filterableViewModel.getMediumFilter();

            int newMediumFilter;
            if (isChecked) {
                newMediumFilter = MediumType.addMediumType(filter, type);
            } else {
                newMediumFilter = MediumType.removeMediumType(filter, type);
            }
            filterableViewModel.setMediumFilter(newMediumFilter);
        });
    }

    abstract Class<ViewModel> getViewModelClass();

    abstract LiveData<PagedList<Value>> createPagedListLiveData();

    abstract List<IFlexible> convertToFlexibles(Collection<Value> list);

    private ViewModel createViewModel() {
        return new ViewModelProvider(this).get(getViewModelClass());
    }
}
