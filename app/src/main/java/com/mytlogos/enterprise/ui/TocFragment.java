package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.mytlogos.enterprise.model.DisplayUnreadEpisode;
import com.mytlogos.enterprise.model.TocPart;
import com.mytlogos.enterprise.viewmodel.MediumViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class TocFragment extends BaseFragment {

    private static final String MEDIUM_ID = "mediumId";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TocFragment() {
    }

    public static TocFragment newInstance(int mediumId) {
        TocFragment fragment = new TocFragment();
        Bundle args = new Bundle();
        args.putInt(MEDIUM_ID, mediumId);
        fragment.setArguments(args);
        return fragment;
    }

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

        MediumViewModel viewModel = ViewModelProviders.of(this).get(MediumViewModel.class);

        Bundle bundle = this.getArguments();
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiper);

        if (bundle != null) {
            LiveData<List<TocPart>> liveToc = viewModel.getToc(bundle.getInt(MEDIUM_ID));
            liveToc.observe(this, mediumItems -> {


                if (checkEmptyList(mediumItems, view, swipeRefreshLayout)) {
                    return;
                }

                List<IFlexible> items = new ArrayList<>();

                for (TocPart item : mediumItems) {
                    for (DisplayUnreadEpisode episode : item.getEpisodes()) {
                        items.add(new ListItem(episode, this));
                    }
                }

                flexibleAdapter.updateDataSet(items);
            });
        }
        // TODO: 22.07.2019 set the mediumTitle
        this.setTitle("Media");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.medium_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_setting) {
            int mediumId = Objects.requireNonNull(this.getArguments()).getInt(MEDIUM_ID);
            MediumSettings fragment = MediumSettings.newInstance(mediumId);
            this.getMainActivity().switchWindow(fragment, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class SectionableListItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final DisplayUnreadEpisode item;
        private final BaseFragment fragment;

        SectionableListItem(@NonNull DisplayUnreadEpisode item, BaseFragment fragment) {
            super(new HeaderItem(item.getTitle()));
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

            ListItem listItem = (ListItem) o;

            return this.item.getEpisodeId() == listItem.item.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.item.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.episode_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            String index;
            if (this.item.getPartialIndex() > 0) {
                index = String.format("#%d.%d", this.item.getTotalIndex(), this.item.getPartialIndex());
            } else {
                index = String.format("#%d", this.item.getTotalIndex());
            }
            holder.textTopLeft.setText(index);
            holder.textTopRight.setText(this.item.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.textContentView.setText(this.item.getTitle());

            holder.mView.setOnClickListener(v -> {
                System.out.println("opening " + this.item.getUrl());
                // TODO: 14.06.2019 open in web or locally
            });
        }

    }

    private static class ListItem extends AbstractFlexibleItem<ViewHolder> {
        private final DisplayUnreadEpisode item;
        private final BaseFragment fragment;

        ListItem(@NonNull DisplayUnreadEpisode item, BaseFragment fragment) {
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

            ListItem listItem = (ListItem) o;

            return this.item.getEpisodeId() == listItem.item.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.item.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.episode_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            String index;
            if (this.item.getPartialIndex() > 0) {
                index = String.format("#%d.%d", this.item.getTotalIndex(), this.item.getPartialIndex());
            } else {
                index = String.format("#%d", this.item.getTotalIndex());
            }
            holder.textTopLeft.setText(index);
            holder.textTopRight.setText(this.item.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.textContentView.setText(this.item.getTitle());

            if (this.item.getUrl() == null || this.item.getUrl().isEmpty()) {
                holder.openBrowserIcon.setAlpha(0.25f);
            }
            if (!this.item.isRead()) {
                holder.episodeReadIcon.setAlpha(0.25f);
            }
            if (!this.item.isSaved()) {
                holder.openLocalIcon.setAlpha(0.25f);
            }
            holder.mView.setOnClickListener(v -> {
                if (this.item.isSaved()) {
                    openLocal();
                } else if (this.item.getUrl() != null && !this.item.getUrl().isEmpty()) {
                    fragment.openInBrowser(this.item.getUrl(), fragment.getContext());
                }
            });
        }

        private void openLocal() {
            File internalBooksDir = this.getInternalAppDir(fragment.getMainActivity().getApplication());
            File externalBooksDir = this.getExternalAppDir();

            String bookZipFile = getBook(internalBooksDir, this.item.getMediumId());

            if (bookZipFile == null) {
                bookZipFile = getBook(externalBooksDir, this.item.getMediumId());
            }
            if (bookZipFile == null) {
                Toast.makeText(fragment.getContext(), "No Book Found", Toast.LENGTH_SHORT).show();
                return;
            }
            ReaderFragment fragment = ReaderFragment.newInstance(this.item.getEpisodeId(), bookZipFile);
            this.fragment.getMainActivity().switchWindow(fragment);
        }

        private String getBook(File dir, int mediumId) {
            if (dir == null) {
                return null;
            }
            for (File file : dir.listFiles()) {
                if (file.getName().matches(mediumId + "\\.epub")) {
                    return file.getAbsolutePath();
                }
            }
            return null;
        }

        private File getExternalAppDir() {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
            }
            return createBookDirectory(Environment.getExternalStorageDirectory());
        }

        private File getInternalAppDir(Application application) {
            return createBookDirectory(application.getFilesDir());
        }

        private File createBookDirectory(File filesDir) {
            File file = new File(filesDir, "Enterprise Books");

            if (!file.exists()) {
                return null;
            }

            return file;
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
        final TextView textContentView;
        private final TextView textTopLeft;
        private final TextView textTopRight;
        private final ImageView episodeReadIcon;
        private final ImageView openBrowserIcon;
        private final ImageView openLocalIcon;
        private DisplayUnreadEpisode mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            this.mView = view;
            this.textTopLeft = view.findViewById(R.id.item_top_left);
            this.textTopRight = view.findViewById(R.id.item_top_right);
            this.episodeReadIcon = view.findViewById(R.id.episode_read);
            this.openBrowserIcon = view.findViewById(R.id.open_in_browser);
            this.openLocalIcon = view.findViewById(R.id.open_local);
            this.textContentView = view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + textContentView.getText() + "'";
        }
    }
}
