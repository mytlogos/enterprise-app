package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.Event;
import com.mytlogos.enterprise.model.WorkerEvent;
import com.mytlogos.enterprise.viewmodel.WorkerEventViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class WorkerViewFragment extends BaseListFragment<WorkerEvent, WorkerEventViewModel> {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Clear All Events").setOnMenuItemClickListener(item -> {
            getViewModel().clearAll();
            return true;
        });
    }

    @Override
    Class<WorkerEventViewModel> getViewModelClass() {
        return WorkerEventViewModel.class;
    }

    @Override
    LiveData<PagedList<WorkerEvent>> createPagedListLiveData() {
        return getViewModel().getEvents();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<WorkerEvent> list) {
        List<IFlexible> flexibles = new ArrayList<>();
        for (WorkerEvent event : list) {
            if (event != null) {
                flexibles.add(new WorkerFlexible(event));
            }
        }
        return flexibles;
    }

    private static class WorkerFlexible extends AbstractFlexibleItem<MetaViewHolder> {
        private final WorkerEvent event;

        private WorkerFlexible(WorkerEvent event) {
            this.event = event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WorkerFlexible that = (WorkerFlexible) o;

            return event.equals(that.event);
        }

        @Override
        public int hashCode() {
            return event.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.meta_item;
        }

        @Override
        public MetaViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new MetaViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position, List<Object> payloads) {
            holder.mainText.setText(this.event.getWorkerName());
            holder.topRightText.setText(this.event.getDateTime().toString("dd.MM.yyyy HH:mm:ss"));
            holder.topLeftText.setText(Event.workerEventToString(this.event.getEvent()));
        }
    }
}
