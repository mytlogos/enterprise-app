package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.SimpleMedium;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.MediumInWaitViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class MediaInWaitFragment extends BaseFragment {

    private static final String MEDIUM_IN_WAIT = "MEDIUM_IN_WAIT";
    private final MediaList NO_LIST = new MediaList("", 0, "Don't add to List", 0, 0);
    private SimpleMedium selectedMedium = null;
    private MediumInWaitViewModel viewModel;
    private MediumInWait mediumInWait;
    private RadioButton addMedium;
    private RadioButton addToc;
    private Spinner listSelect;
    private View listSelectContainer;
    private boolean running;
    private SearchView searchMediumView;
    private SearchView searchMediumInWaitView;
    private final Set<MediumInWait> selectedInWaits = new HashSet<>();

    public MediaInWaitFragment() {

    }

    static MediaInWaitFragment getInstance(MediumInWait inWait) {
        MediaInWaitFragment fragment = new MediaInWaitFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEDIUM_IN_WAIT, inWait);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medium_in_wait, container, false);

        Bundle bundle = requireArguments();
        mediumInWait = (MediumInWait) bundle.getSerializable(MediaInWaitFragment.MEDIUM_IN_WAIT);

        if (mediumInWait == null) {
            throw new IllegalArgumentException("no arguments");
        }
        TextView titleView = view.findViewById(R.id.title);
        String domain = Utils.getDomain(this.mediumInWait.getLink());

        titleView.setText(String.format("%s (%s)", this.mediumInWait.getTitle(), domain));

        viewModel = new ViewModelProvider(this).get(MediumInWaitViewModel.class);

        FlexibleAdapter<IFlexible> listAdapter = getFlexibleRecyclerAdapter(view, R.id.list);
        FlexibleAdapter<IFlexible> mediumSuggestAdapter = getFlexibleRecyclerAdapter(view, R.id.medium_suggestions);
        FlexibleAdapter<IFlexible> mediumInWaitSuggestAdapter = getFlexibleRecyclerAdapter(view, R.id.medium_in_wait_suggestions);

        mediumSuggestAdapter.addListener((FlexibleAdapter.OnItemClickListener) (view1, position) -> {
            IFlexible flexible = mediumSuggestAdapter.getItem(position);
            if (!(flexible instanceof FlexibleMedium)) {
                return false;
            }
            FlexibleMedium medium = (FlexibleMedium) flexible;
            this.selectedMedium = medium.medium;
            this.searchMediumView.setQuery(medium.medium.getTitle(), false);
            return false;
        });
        mediumInWaitSuggestAdapter.addListener((FlexibleAdapter.OnItemClickListener) (view1, position) -> {
            IFlexible flexible = mediumInWaitSuggestAdapter.getItem(position);
            if (!(flexible instanceof FlexibleMediumInWaitSuggestion)) {
                return false;
            }
            FlexibleMediumInWaitSuggestion medium = (FlexibleMediumInWaitSuggestion) flexible;
            MediumInWaitSimpleFilter filter = mediumInWaitSuggestAdapter.getFilter(MediumInWaitSimpleFilter.class);

            if (filter == null) {
                filter = new MediumInWaitSimpleFilter();
            }
            filter.filterOut.add(medium.mediumInWait);
            this.selectedInWaits.add(medium.mediumInWait);
            listAdapter.addItem(new FlexibleMediumInWait(medium.mediumInWait));

            mediumInWaitSuggestAdapter.setFilter(filter);
            mediumInWaitSuggestAdapter.filterItems();
            return false;
        });

        searchMediumView = view.findViewById(R.id.search_medium_view);
        searchMediumView.setOnQueryTextListener(searchMediumListener());

        searchMediumInWaitView = view.findViewById(R.id.search_medium_in_wait_view);
        searchMediumInWaitView.setOnQueryTextListener(searchMediumInWaitListener());

        listSelect = view.findViewById(R.id.list_select);
        listSelectContainer = view.findViewById(R.id.list_select_container);
        listSelect.setAdapter(new TextOnlyListAdapter<>(
                this,
                Transformations.map(viewModel.getInternalLists(), input -> {
                    input.add(0, NO_LIST);
                    return input;
                }),
                MediaList::getName
        ));

        addMedium = view.findViewById(R.id.add_medium);
        addToc = view.findViewById(R.id.add_toc);

        switchAddMode(searchMediumView, addMedium.isChecked());

        addMedium.setOnCheckedChangeListener((buttonView, isChecked) -> switchAddMode(searchMediumView, isChecked));

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> requireActivity().onBackPressed());
        view.findViewById(R.id.add_btn).setOnClickListener(v -> process(listAdapter));

        LiveData<List<MediumInWait>> similarMediaInWait = viewModel.getSimilarMediaInWait(mediumInWait);
        similarMediaInWait.observe(this, mediumInWaits -> {
            List<IFlexible> list = new ArrayList<>();

            mediumInWaits.addAll(this.selectedInWaits);

            for (MediumInWait inWait : mediumInWaits) {
                FlexibleMediumInWait flexible = new FlexibleMediumInWait(inWait);

                if (!flexible.mediumInWait.equals(this.mediumInWait)) {
                    list.add(flexible);
                }
            }
            listAdapter.updateDataSet(list);
        });

        LiveData<List<MediumInWait>> inWaitSuggestions = viewModel.getMediumInWaitSuggestions(mediumInWait.getMedium());
        inWaitSuggestions.observe(this, mediumInWaits -> mediumInWaitSuggestAdapter.updateDataSet(
                mediumInWaits.stream().map(FlexibleMediumInWaitSuggestion::new).collect(Collectors.toList())
        ));

        LiveData<List<SimpleMedium>> mediumSuggestions = viewModel.getMediumSuggestions(mediumInWait.getMedium());
        mediumSuggestions.observe(this, medium -> mediumSuggestAdapter.updateDataSet(
                medium.stream().map(FlexibleMedium::new).collect(Collectors.toList())
        ));

        return view;
    }

    private static class MediumInWaitSimpleFilter implements Serializable {
        private final Set<MediumInWait> filterOut = new HashSet<>();
    }


    private SearchView.OnQueryTextListener searchMediumListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setMediumTitleFilter(newText);
                return true;
            }
        };
    }

    private SearchView.OnQueryTextListener searchMediumInWaitListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setMediumInWaitTitleFilter(newText);
                return true;
            }
        };
    }

    private void switchAddMode(SearchView searchMediumView, boolean addMedium) {
        if (addMedium) {
            listSelectContainer.setVisibility(View.VISIBLE);
            searchMediumView.setVisibility(View.GONE);
        } else {
            listSelectContainer.setVisibility(View.GONE);
            searchMediumView.setVisibility(View.VISIBLE);
        }
    }

    private void process(FlexibleAdapter<IFlexible> listAdapter) {
        if (this.running) {
            return;
        }
        this.running = true;

        if (addMedium.isChecked()) {
            List<IFlexible> items = listAdapter.getCurrentItems();
            List<MediumInWait> mediumInWaits = new ArrayList<>();

            for (IFlexible item : items) {
                if (item instanceof FlexibleMediumInWait) {
                    mediumInWaits.add(((FlexibleMediumInWait) item).mediumInWait);
                }
            }
            MediaList item = (MediaList) listSelect.getSelectedItem();

            if (Objects.equals(item, NO_LIST)) {
                item = null;
            }

            CompletableFuture<Boolean> success = viewModel.createMedium(mediumInWait, mediumInWaits, item);
            success.whenComplete((aBoolean, throwable) -> {
                String msg;
                if (aBoolean == null || !aBoolean || throwable != null) {
                    msg = "Could not create Medium";
                } else {
                    msg = "Created a Medium and consumed " + mediumInWaits.size() + " other unused Media";
                }
                this.running = false;
                requireActivity().runOnUiThread(() -> {
                    showToast(msg);
                    requireActivity().onBackPressed();
                });
            });
        } else if (addToc.isChecked()) {
            if (this.selectedMedium == null) {
                showToast("No Medium selected");
                return;
            }
            List<IFlexible> items = listAdapter.getCurrentItems();
            List<MediumInWait> mediumInWaits = new ArrayList<>();

            for (IFlexible item : items) {
                if (item instanceof FlexibleMediumInWait) {
                    mediumInWaits.add(((FlexibleMediumInWait) item).mediumInWait);
                }
            }
            mediumInWaits.add(this.mediumInWait);

            CompletableFuture<Boolean> success = viewModel.consumeMediumInWait(this.selectedMedium, mediumInWaits);
            success.whenComplete((aBoolean, throwable) -> {
                String msg;
                if (aBoolean == null || !aBoolean || throwable != null) {
                    msg = "Could not process Media";
                } else {
                    msg = "Consumed " + mediumInWaits.size() + " Media";
                }
                this.running = false;
                requireActivity().runOnUiThread(() -> {
                    showToast(msg);
                    requireActivity().onBackPressed();
                });
            });
        }
    }

    private FlexibleAdapter<IFlexible> getFlexibleRecyclerAdapter(View view, int id) {
        RecyclerView recyclerView = view.findViewById(id);
        // Set the adapter
        Context context = view.getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        FlexibleAdapter<IFlexible> adapter = new FlexibleAdapter<>(null);
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    private static class FlexibleMediumInWait extends AbstractFlexibleItem<ViewHolder> {
        private final MediumInWait mediumInWait;

        private FlexibleMediumInWait(MediumInWait mediumInWait) {
            this.mediumInWait = mediumInWait;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlexibleMediumInWait that = (FlexibleMediumInWait) o;

            return mediumInWait.equals(that.mediumInWait);
        }

        @Override
        public int hashCode() {
            return mediumInWait.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.closeable_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            String domain = Utils.getDomain(this.mediumInWait.getLink());

            String title = String.format("%s (%s)", this.mediumInWait.getTitle(), domain);
            holder.textView.setText(title);
        }
    }

    private static class FlexibleMedium extends AbstractFlexibleItem<ViewHolder> {
        private final SimpleMedium medium;

        private FlexibleMedium(SimpleMedium medium) {
            this.medium = medium;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlexibleMedium that = (FlexibleMedium) o;

            return medium.equals(that.medium);
        }

        @Override
        public int hashCode() {
            return medium.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.text_only_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.textView.setText(this.medium.getTitle());
        }
    }

    private static class FlexibleMediumInWaitSuggestion extends AbstractFlexibleItem<ViewHolder> implements IFilterable<MediumInWaitSimpleFilter> {
        private final MediumInWait mediumInWait;

        private FlexibleMediumInWaitSuggestion(MediumInWait mediumInWait) {
            this.mediumInWait = mediumInWait;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlexibleMediumInWaitSuggestion that = (FlexibleMediumInWaitSuggestion) o;

            return mediumInWait.equals(that.mediumInWait);
        }

        @Override
        public int hashCode() {
            return mediumInWait.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.text_only_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            String domain = Utils.getDomain(this.mediumInWait.getLink());

            String title = String.format("%s (%s)", this.mediumInWait.getTitle(), domain);
            holder.textView.setText(title);
        }

        @Override
        public boolean filter(MediumInWaitSimpleFilter constraint) {
            if (constraint == null) {
                return true;
            }
            return !constraint.filterOut.contains(this.mediumInWait);
        }
    }

    private static class ViewHolder extends FlexibleViewHolder {
        private TextView textView;

        private ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            textView = view.findViewById(R.id.text);
        }
    }
}
