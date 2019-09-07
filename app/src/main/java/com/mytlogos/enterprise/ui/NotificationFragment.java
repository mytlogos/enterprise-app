package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.NotificationItem;
import com.mytlogos.enterprise.viewmodel.NotificationViewModel;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class NotificationFragment extends BaseListFragment<NotificationItem, NotificationViewModel> {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setTitle("Notification History");
        return view;
    }

    @Override
    Class<NotificationViewModel> getViewModelClass() {
        return NotificationViewModel.class;
    }

    @Override
    LiveData<PagedList<NotificationItem>> createPagedListLiveData() {
        return getViewModel().getNotifications();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<NotificationItem> list) {
        return list
                .stream()
                .filter(Objects::nonNull)
                .map(FlexibleNotification::new)
                .collect(Collectors.toList());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.notification_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clear_notifications) {
            getViewModel().clearNotifications();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class FlexibleNotification extends AbstractFlexibleItem<MetaViewHolder> {
        private final NotificationItem item;

        private FlexibleNotification(NotificationItem item) {
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlexibleNotification that = (FlexibleNotification) o;

            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return item != null ? item.hashCode() : 0;
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
            holder.mainText.setText(this.item.getTitle());
            holder.topLeftText.setText(this.item.getDateTime().toString("dd.MM.yyyy HH:mm:ss"));
        }
    }
}
