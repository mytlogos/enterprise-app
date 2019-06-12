package com.mytlogos.enterprise.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.TimeAgo;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.viewmodel.MediumViewModel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediumFragment extends BaseFragment {

    private MediumViewModel viewModel;
    private LiveData<List<MediumItem>> liveMedia;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_list, container, false);

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

        this.viewModel = ViewModelProviders.of(this).get(MediumViewModel.class);

        this.liveMedia = this.viewModel.getAllMedia();
        this.liveMedia.observe(this, mediumItems -> {
            if (mediumItems != null) {
                List<IFlexible> items = new ArrayList<>();

                for (MediumItem item : mediumItems) {
                    items.add(new ListItem(item, this));
                }

                flexibleAdapter.updateDataSet(items);
            }
        });
        this.setTitle("Media");
        return view;
    }

    private static class SectionableListItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final MediumItem item;
        private final BaseFragment fragment;

        SectionableListItem(@NonNull MediumItem item, BaseFragment fragment) {
            super(new HeaderItem(item.getAuthor()));
            this.item = item;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableListItem)) return false;

            SectionableListItem other = (SectionableListItem) o;
            return this.item.getMediumId() == other.item.getMediumId();
        }

        @Override
        public int hashCode() {
            return this.item.getMediumId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.list_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(String.format("%d/%d", this.item.getCurrentReadEpisode(), this.item.getLastEpisode()));

            CharSequence relativeTime;
            DateTime lastUpdated = this.item.getLastUpdated();

            if (lastUpdated != null) {
                relativeTime = TimeAgo.toRelative(lastUpdated, DateTime.now());
            } else {
                relativeTime = "No Updates";
            }

            holder.denominatorView.setText(relativeTime);
            holder.contentView.setText(this.item.getTitle());

            holder.mView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt(MediumSettings.ID, this.item.getMediumId());
                fragment.getMainActivity().switchWindow(new MediumSettings(), bundle, true);
            });
        }

    }

    private static class ListItem extends AbstractFlexibleItem<ViewHolder> {
        private final MediumItem item;
        private final BaseFragment fragment;

        ListItem(@NonNull MediumItem item, BaseFragment fragment) {
            this.item = item;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableListItem)) return false;

            SectionableListItem other = (SectionableListItem) o;
            return this.item.getMediumId() == other.item.getMediumId();
        }

        @Override
        public int hashCode() {
            return this.item.getMediumId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.news_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            Integer currentReadEpisode = this.item.getCurrentReadEpisode() == null ? 0 : this.item.getCurrentReadEpisode();
            Integer lastEpisode = this.item.getLastEpisode() == null ? 0 : this.item.getLastEpisode();
            holder.metaView.setText(String.format("%d/%d", currentReadEpisode, lastEpisode));

            CharSequence relativeTime;
            DateTime lastUpdated = this.item.getLastUpdated();

            if (lastUpdated != null) {
                relativeTime = TimeAgo.toRelative(lastUpdated, DateTime.now());
            } else {
                relativeTime = "No Updates";
            }

            holder.denominatorView.setText(relativeTime);
            holder.contentView.setText(this.item.getTitle());

            holder.mView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt(MediumSettings.ID, this.item.getMediumId());
                fragment.getMainActivity().switchWindow(new MediumSettings(), bundle, true);
            });
        }

    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {

        private final String title;

        private HeaderItem(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HeaderItem that = (HeaderItem) o;

            return title.equals(that.title);
        }

        @Override
        public int hashCode() {
            return title.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.flexible_header;
        }

        @Override
        public HeaderViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new HeaderViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, HeaderViewHolder holder, int position, List<Object> payloads) {
            holder.textView.setText(this.title);
        }
    }

    private static class HeaderViewHolder extends FlexibleViewHolder {
        private TextView textView;

        HeaderViewHolder(@NonNull View itemView, FlexibleAdapter<IFlexible> adapter) {
            super(itemView, adapter, true);
            textView = itemView.findViewById(R.id.text);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView denominatorView;
        MediumItem mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            metaView = view.findViewById(R.id.item_meta);
            denominatorView = view.findViewById(R.id.item_novel);
            contentView = view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
