package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.viewmodel.ListMediaViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

public class ListMediumFragment extends BaseFragment {
    static final String ID = "id";
    static final String TITLE = "listTitle";
    static final String EXTERNAL = "external";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.normal_list, container, false);

        Bundle arguments = Objects.requireNonNull(this.getArguments());
        int listId = arguments.getInt(ID);
        boolean isExternal = arguments.getBoolean(EXTERNAL);
        String listTitle = arguments.getString(TITLE);

        RecyclerView recyclerView = view.findViewById(R.id.list);

        // Set the adapter
        Context context = view.getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        FlexibleAdapter<IFlexible> flexibleAdapter = new FlexibleAdapter<>(null)
                .setStickyHeaders(true)
                .setDisplayHeadersAtStartUp(true);

        recyclerView.setAdapter(flexibleAdapter);

        ListMediaViewModel viewModel = ViewModelProviders.of(this).get(ListMediaViewModel.class);

        LiveData<List<MediumItem>> liveMedia = viewModel.getMedia(listId, isExternal);
        liveMedia.observe(this, mediumItems -> {

            if (checkEmptyList(mediumItems, view, recyclerView)) {
                return;
            }

            List<IFlexible> items = new ArrayList<>();

            for (MediumItem item : mediumItems) {
                items.add(new MediumFragment.FlexibleMediumItem(item, this));
            }

            flexibleAdapter.updateDataSet(items);
        });
        this.setTitle(listTitle);
        this.setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Fragment fragment = null;
        Bundle bundle = null;
        boolean selected = false;

        if (item.getItemId() == R.id.item_setting) {
            fragment = new ListSettings();
            bundle = Objects.requireNonNull(this.getArguments());
            selected = true;
        }

        if (fragment != null) {
            this.getMainActivity().switchWindow(fragment, bundle, true);
        }
        if (selected) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
