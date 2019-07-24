package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.viewmodel.MediumInWaitViewModel;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class MediaInWaitListFragment extends BaseFragment {

    private MediumInWaitViewModel viewModel;
    private LiveData<List<MediumInWait>> liveMedia;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        this.viewModel = ViewModelProviders.of(this).get(MediumInWaitViewModel.class);
        this.swipeRefreshLayout = view.findViewById(R.id.swiper);
        this.swipeRefreshLayout.setOnRefreshListener(() -> new LoadingTask().execute());

        this.liveMedia = this.viewModel.getAllMediaInWait();
        this.liveMedia.observe(this, mediumItems -> {

            if (checkEmptyList(mediumItems, view, this.swipeRefreshLayout)) {
                return;
            }

            List<IFlexible> items = new ArrayList<>();

            for (MediumInWait item : mediumItems) {
                items.add(new MediumItem(item, this));
            }

            flexibleAdapter.updateDataSet(items);
        });
        this.setTitle("Unused Media");
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        private String errorMsg = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                viewModel.loadMediaInWait();
            } catch (IOException e) {
                errorMsg = "Loading went wrong";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (this.errorMsg != null) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    public static class MediumItem extends AbstractFlexibleItem<ViewHolder> {
        private final MediumInWait item;
        private final BaseFragment fragment;

        MediumItem(@NonNull MediumInWait item, BaseFragment fragment) {
            this.item = item;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediumItem that = (MediumItem) o;

            if (!item.equals(that.item)) return false;
            return fragment.equals(that.fragment);
        }

        @Override
        public int hashCode() {
            int result = item.hashCode();
            result = 31 * result + fragment.hashCode();
            return result;
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

            String mediumType;

            switch (this.item.getMedium()) {
                case MediumType.AUDIO:
                    mediumType = "Audio";
                    break;
                case MediumType.IMAGE:
                    mediumType = "Bild";
                    break;
                case MediumType.TEXT:
                    mediumType = "Text";
                    break;
                case MediumType.VIDEO:
                    mediumType = "Video";
                    break;
                default:
                    String msg = String.format("no valid medium type: %d", this.item.getMedium());
                    throw new IllegalStateException(msg);
            }
            holder.metaView.setText(mediumType);

            String host = URI.create(this.item.getLink()).getHost();
            Matcher matcher = Pattern.compile("(www\\.)?(.+?)/?").matcher(host);

            String domain;
            if (matcher.matches()) {
                domain = matcher.group(2);
                int index = domain.indexOf("/");

                if (index >= 0) {
                    domain = domain.substring(0, index);
                }
            } else {
                domain = host;
            }
            holder.denominatorView.setText(domain);
            holder.contentView.setText(this.item.getTitle());

            holder.mView.setOnClickListener(v -> {
//                this.fragment.getMainActivity().switchWindow(fragment, true);
            });
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView denominatorView;
        MediumInWait mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            metaView = view.findViewById(R.id.item_top_left);
            denominatorView = view.findViewById(R.id.item_top_right);
            contentView = view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
